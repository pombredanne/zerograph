#!/usr/bin/env python

from itertools import cycle, islice
import json
import logging
from weakref import WeakValueDictionary

import yaml
try:
    from yaml import CLoader as Loader
except ImportError:
    from yaml import Loader
import zmq


log = logging.getLogger(__name__)
log.addHandler(logging.NullHandler())


GET = "GET"
SET = "SET"
PATCH = "PATCH"
CREATE = "CREATE"
DELETE = "DELETE"
EXECUTE = "EXECUTE"


def assert_bound(obj):
    if not isinstance(obj, Bindable):
        raise ValueError("Object is not bindable")
    obj.assert_bound()


def is_safe_char(x):
    return x.isalpha() or x.isdigit() or x == "_"


def round_robin(*iterables):
    """ Cycle through a number of iterables, returning the next item from each
    in turn::

        >>> list(round_robin('ABC', 'D', 'EF'))
        ['A', 'D', 'E', 'B', 'F', 'C']

    Original recipe credited to George Sakkis; Python 2/3 cross-compatibility
    tweak by Nigel Small
    """
    pending = len(iterables)
    nexts = cycle(iter(it) for it in iterables)
    while pending:
        try:
            for n in nexts:
                yield next(n)
        except StopIteration:
            pending -= 1
            nexts = cycle(islice(nexts, pending))


class ZerographEncoder(json.JSONEncoder):

    def encode(self, o):
        if isinstance(o, dict):
            o_ = {}
            for key, value in o.items():
                if isinstance(value, Pointer):
                    o_[key + "*"] = value.address
                else:
                    o_[key] = value
            o = o_
        return json.JSONEncoder.encode(self, o)


class Error(Exception):
    pass


class UnboundError(Exception):
    pass


class Request(object):

    def __init__(self, method, resource, **arguments):
        self.__method = method
        self.__resource = resource
        self.__arguments = arguments

    @property
    def method(self):
        return self.__method

    @property
    def resource(self):
        return self.__resource

    def argument(self, name):
        return self.__arguments.get(name)

    def send(self, socket, more=False):
        line = " ".join((self.__method, self.__resource,
                         json.dumps(self.__arguments, separators=",:",
                                    ensure_ascii=True, cls=ZerographEncoder)))
        socket.send(line.encode("utf-8"), zmq.SNDMORE if more else 0)


class Response(object):

    @classmethod
    def receive(cls, graph):

        class GraphLoader(Loader):
            __graph__ = graph

        full = []
        more = True
        while more:
            try:
                frame = graph.socket.recv(copy=False)
            except zmq.error.ZMQError as err:
                raise TimeoutError("Timeout occurred while trying to receive "
                                   "data")
            else:
                full.append(frame.bytes.decode("utf-8"))
                more = frame.more
        if full:
            for document in yaml.load_all("".join(full), Loader=GraphLoader):
                yield Response(document or {})
        else:
            yield Response({})

    def __init__(self, document):
        self.__document = document
        self.__head = None
        self.__body = None
        self.__foot = None
        if "error" in document:
            raise Error(document["error"])

    def __repr__(self):
        s = ["Response"]
        if self.head:
            s.append("head={0}".format(repr(self.head)))
        if self.body:
            s.append("body={0}".format(repr(self.body)))
        if self.foot:
            s.append("foot={0}".format(repr(self.foot)))
        return "<" + " ".join(s) + ">"

    @property
    def head(self):
        if self.__head is None:
            self.__head = self.__document.get("head")
        return self.__head

    @property
    def body(self):
        if self.__body is None:
            self.__body = self.__document.get("body")
        return self.__body

    @property
    def foot(self):
        if self.__foot is None:
            self.__foot = self.__document.get("foot")
        return self.__foot

    def to_table(self):
        return Table(self.head["columns"], self.body)


class Table(object):

    def __init__(self, columns, rows):
        self.__columns = list(columns)
        self.__rows = list(rows)

    def __repr__(self):
        column_widths = [len(column) for column in self.__columns]
        for row in self.__rows:
            for i, value in enumerate(row):
                column_widths[i] = max(column_widths[i], len(str(value)))
        out = [" " + " | ".join(
            column.ljust(column_widths[i])
            for i, column in enumerate(self.__columns)
        ) + " "]
        out += ["-" + "-+-".join(
            "-" * column_widths[i]
            for i, column in enumerate(self.__columns)
        ) + "-"]
        for row in self.__rows:
            out.append(" " + " | ".join(str(value).ljust(column_widths[i])
                                        for i, value in enumerate(row)) + " ")
        out = "\n".join(out)
        if len(self.__rows) == 1:
            out += "\n(1 row)\n"
        else:
            out += "\n({0} rows)\n".format(len(self.__rows))
        return out

    def __len__(self):
        return len(self.__rows)

    def __getitem__(self, index):
        return self.__rows[index]

    def __iter__(self):
        return iter(self.__rows)

    @property
    def columns(self):
        return self.__columns

    @property
    def rows(self):
        return self.__rows


class Batch(object):

    @classmethod
    def single(cls, graph, method, *args, **kwargs):
        """ Execute a single request.
        """
        batch = cls(graph)
        method(batch, *args, **kwargs)
        results = batch.submit()
        result = next(results)
        return result

    def __init__(self, graph):
        self.__graph = graph
        self.__socket = self.__graph.socket
        self.__count = 0

    def append(self, method, resource, **args):
        """ Append a request.
        """
        adjusted = {}
        for key, value in args.items():
            if isinstance(value, Pointer):
                adjusted[key + "*"] = value.address
            elif value is not None:
                adjusted[key] = value
        Request(method, resource, **adjusted).send(self.__socket, more=True)
        pointer = Pointer(self.__count)
        self.__count += 1
        return pointer

    def submit(self):
        # Maybe stream this properly one day if we can have one socket
        # per batch :-/
        self.__socket.send(b"")  # to close multipart message
        values = []
        for result in Response.receive(self.__graph):
            # interpret the result type (should this be explicit?)
            if isinstance(result.body, list):
                if result.head and "columns" in result.head:
                    value = result.to_table()
                else:
                    value = iter(result.body)
            else:
                value = result.body
            values.append(value)
        while len(values) < self.__count:
            values.append(None)
        self.__count = 0
        return iter(values)

    ### Cypher ###

    def execute_cypher(self, query, *param_sets):
        param_set_count = len(param_sets)
        if param_set_count == 0:
            return self.append(EXECUTE, "Cypher", query=query)
        elif param_set_count == 1:
            return self.append(EXECUTE, "Cypher", query=query,
                               params=dict(param_sets[0]))
        else:
            pointers = []
            for param_set in param_sets:
                pointers.append(self.append(EXECUTE, "Cypher", query=query,
                                            params=dict(param_set)))
            return pointers

    ### Graph ###

    def get_graph(self, host, port):
        return self.append(GET, "Graph", host=host, port=int(port))

    def patch_graph(self, host, port):
        return self.append(PATCH, "Graph", host=host, port=int(port))

    def delete_graph(self, host, port):
        return self.append(DELETE, "Graph", host=host, port=int(port))

    ### Node ###

    def get_node(self, id):
        return self.append(GET, "Node", id=id)

    def set_node(self, id, labels, properties):
        return self.append(SET, "Node", id=id, labels=list(labels or []),
                           properties=dict(properties or {}))

    def patch_node(self, id, labels, properties):
        return self.append(PATCH, "Node", id=id, labels=list(labels or []),
                           properties=dict(properties or {}))

    def create_node(self, labels=None, properties=None):
        return self.append(CREATE, "Node", labels=list(labels or []),
                           properties=dict(properties or {}))

    def delete_node(self, id):
        return self.append(DELETE, "Node", id=id)

    ### NodeSet ###

    def get_node_set(self, label, key=None, value=None):
        if key is None:
            return self.append(GET, "NodeSet", label=label)
        else:
            return self.append(GET, "NodeSet", label=label, key=key,
                               value=value)

    def patch_node_set(self, label, key, value):
        return self.append(PATCH, "NodeSet", label=label, key=key,
                           value=value)

    def delete_node_set(self, label, key, value):
        if key is None:
            return self.append(DELETE, "NodeSet", label=label)
        else:
            return self.append(DELETE, "NodeSet", label=label, key=key,
                               value=value)

    ### Rel ###

    def get_rel(self, id):
        return self.append(GET, "Rel", id=id)

    def set_rel(self, id, properties):
        return self.append(SET, "Rel", id=id,
                           properties=dict(properties or {}))

    def patch_rel(self, id, properties):
        return self.append(PATCH, "Rel", id=id,
                           properties=dict(properties or {}))

    def create_rel(self, start, end, type, properties=None):
        return self.append(CREATE, "Rel", start=start, end=end, type=type,
                           properties=dict(properties or {}))

    def delete_rel(self, id):
        return self.append(DELETE, "Rel", id=id)

    ### RelSet ###

    def get_rel_set(self, start=None, end=None, type=None):
        if start is None and end is None:
            raise ValueError("Either start or end node must be specified")
        return self.append(GET, "RelSet", start=start, end=end, type=type)

    def patch_rel_set(self, start, end, type):
        return self.append(PATCH, "RelSet", start=start, end=end, type=type)

    def delete_rel_set(self, start=None, end=None, type=None):
        if start is None and end is None:
            raise ValueError("Either start or end node must be specified")
        return self.append(DELETE, "RelSet", start=start, end=end, type=type)

    ### Zerograph ###
    
    def get_zerograph(self):
        return self.append(GET, "Zerograph")


class Pointer(object):

    def __init__(self, attributes):
        self.__address = attributes

    def __repr__(self):
        return "<Pointer address={0}>".format(self.address)

    def __eq__(self, other):
        return self.address == other.address

    def __ne__(self, other):
        return not self.__eq__(other)

    @property
    def address(self):
        return self.__address


class Graph(yaml.YAMLObject):
    """ A `Graph` instance holds a connection to a remote graph database
    service.
    """
    yaml_tag = '!Graph'

    ZEROGRAPH_PORT = 47470

    __services = {}
    __nodes = WeakValueDictionary()
    __rels = WeakValueDictionary()

    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        host = mapping.get("host")
        port = mapping.get("port")
        inst = cls(host=host, port=port)
        return inst

    @classmethod
    def open(cls, host="localhost", port=ZEROGRAPH_PORT):
        """ Open a connection to a remote graph, creating a database on that
        port if none is available.
        """
        host_port = (host, port)
        try:
            return cls.__services[host_port]
        except KeyError:
            if port == cls.ZEROGRAPH_PORT:
                graph = cls(host, port)
            else:
                zerograph = cls.open(host)
                graph = Batch.single(zerograph, Batch.patch_graph,
                                     zerograph.host, port)
            cls.__services[host_port] = graph
            return graph

    @classmethod
    def drop(cls, host, port):
        """ Close the graph database service on the host and port specified and
        destroy the database behind it.
        """
        host_port = (host, port)
        if port == cls.ZEROGRAPH_PORT:
            raise ValueError("Cannot drop zerograph")
        else:
            zerograph = cls.open(host)
            Batch.single(zerograph, Batch.delete_graph, zerograph.host, port)
            del cls.__services[host_port]

    def __init__(self, host, port):
        self.__host = host
        self.__port = port
        self.__address = "tcp://{0}:{1}".format(self.__host, self.__port)
        self.__context = zmq.Context()
        self.__socket = self.__context.socket(zmq.REQ)
        self.__socket.setsockopt(zmq.RCVTIMEO, 120000)  # TODO: configurable timeout
        try:
            self.__socket.connect(self.__address)
        except zmq.error.ZMQError as err:
            raise TimeoutError("Timeout occurred while trying to connect to "
                               "{0} on port {1}".format(self.__host,
                                                        self.__port))

    def __repr__(self):
        return "<Graph host={0} port={1}>".format(self.__host, self.__port)

    @property
    def host(self):
        """ The remote host name for this graph database service.
        """
        return self.__host

    @property
    def port(self):
        """ The remote port number for this graph database service.
        """
        return self.__port

    @property
    def address(self):
        return self.__address

    @property
    def socket(self):
        return self.__socket

    @property
    def zerograph(self):
        """ The graph database service running on port 47470 associated with
        this graph.
        """
        if self.__port == self.ZEROGRAPH_PORT:
            return self
        else:
            return self.open(self.__host)

    @property
    def order(self):
        # TODO: count all nodes (V1)
        return None

    @property
    def size(self):
        # TODO: count all rels (V1)
        return None

    def clear(self):
        # TODO (V1)
        pass

    def execute(self, query, *param_sets):
        """ Execute a Cypher query, optionally multiple times with several
        parameter sets.
        """
        param_set_count = len(param_sets)
        if param_set_count == 0:
            return Batch.single(self, Batch.execute_cypher, query)
        elif param_set_count == 1:
            return Batch.single(self, Batch.execute_cypher, query,
                                param_sets[0])
        else:
            batch = Batch(self)
            for param_set in param_sets:
                batch.execute_cypher(query, param_set)
            results = batch.submit()
            return list(results)

    def bind_node(self, node_id, node):
        try:
            cached = self.__nodes[node_id]
            cached.replace(*node.labels, **node.properties)
            return cached
        except KeyError:
            node.bind(self, id=node_id)
            self.__nodes[node_id] = node
            return node

    def bind_rel(self, rel_id, rel):
        try:
            cached = self.__rels[rel_id]
            cached.replace(rel.type, **rel.properties)
            return cached
        except KeyError:
            rel.bind(self, id=rel_id)
            self.__rels[rel_id] = rel
            return rel

    def node(self, node_id):
        """ Fetch a :class:`Node` by internal ID.
        """
        try:
            return self.__nodes[node_id]
        except KeyError:
            # If we need to fetch a copy because no cached copy exists, this
            # will get inserted into the cache when the node is bound in
            # `bind_node`, above.
            return Batch.single(self, Batch.get_node, node_id)

    def relationship(self, rel_id):
        """ Fetch a :class:`Relationship` by internal ID.
        """
        try:
            return self.__rels[rel_id]
        except KeyError:
            # If we need to fetch a copy because no cached copy exists, this
            # will get inserted into the cache when the rel is bound in
            # `bind_rel`, above.
            return Batch.single(self, Batch.get_rel, rel_id)

    def pull(self, *entities):
        """ Update multiple local entities from remote entities.
        """
        batch = BatchPull(self)
        for entity in entities:
            batch.add(entity)
        batch.submit()

    def push(self, *entities):
        """ Update multiple remote entities from local entities.
        """
        batch = BatchPush(self)
        for entity in entities:
            batch.add(entity)
        batch.submit()

    def create(self, *entities):
        """ Create multiple remote entities.
        """
        batch = BatchCreate(self)
        for entity in entities:
            batch.add(entity)
        return batch.submit()

    def delete(self, *entities):
        """ Delete multiple remote entities.
        """
        # TODO
        pass

    def find(self, label, key=None, value=None):
        return Batch.single(self, Batch.get_node_set, label, key, value)


class Bindable(object):
    """ Mixin for objects that can be bound to a remote graph.
    """

    def __init__(self):
        self.__graph = None

    @property
    def graph(self):
        """ Returns the :class:`Graph` to which this is bound.
        """
        return self.__graph

    @property
    def bound(self):
        """ Returns :const:`True` if bound to a remote graph database.
        """
        return self.__graph is not None

    def bind(self, graph, **kwargs):
        self.__graph = graph

    def unbind(self):
        self.__graph = None

    def assert_bound(self):
        if not self.bound:
            raise UnboundError(self)

    def pull(self):
        pass

    def push(self):
        pass


class Entity(Bindable):
    """ Mixin for objects that can be bound to single entities within a remote
    graph database.
    """

    def __init__(self):
        Bindable.__init__(self)
        self.__id = None

    @property
    def _id(self):
        """ The internal ID of the remote entity to which this is bound.
        """
        return self.__id

    def bind(self, graph, **kwargs):
        Bindable.bind(self, graph)
        self.__id = kwargs["id"]

    def unbind(self):
        Bindable.unbind(self)
        self.__id = None


class PropertySet(dict):
    """ A dict subclass that equates None with a non-existent key.
    """

    def __init__(self, iterable=None, **kwargs):
        dict.__init__(self)
        self.update(iterable, **kwargs)

    def __getitem__(self, key):
        return dict.get(self, key)

    def __setitem__(self, key, value):
        if value is None:
            try:
                dict.__delitem__(self, key)
            except KeyError:
                pass
        else:
            dict.__setitem__(self, key, value)

    def __eq__(self, other):
        return dict(self) == dict(PropertySet(other))

    def __ne__(self, other):
        return not self.__eq__(other)

    def __len__(self):
        return dict.__len__(self)

    def __bool__(self):
        return dict.__len__(self) > 0

    def __nonzero__(self):
        return dict.__len__(self) > 0

    def to_cypher(self, **kwargs):
        s = []
        for key in sorted(self.keys()):
            if s:
                s += ","
            if all(map(is_safe_char, key)):
                s.append(key)
            else:
                s.append("`")
                s.append(key.replace("`", "``"))
                s.append("`")
            s.append(":")
            s.append(json.dumps(self[key], separators=",:", sort_keys=True))
        s = ["{"] + s + ["}"]
        return "".join(s)

    def to_json(self):
        return json.dumps(self, separators=",:", sort_keys=True)

    def setdefault(self, key, default=None):
        if key in self:
            return self[key]
        elif default is None:
            return None
        else:
            return dict.setdefault(self, key, default)

    def update(self, iterable=None, **kwargs):
        if iterable:
            try:
                for key in iterable.keys():
                    self[key] = iterable[key]
            except (AttributeError, TypeError):
                for key, value in iterable:
                    self[key] = value
        for key in kwargs:
            self[key] = kwargs[key]


class PropertyContainer(object):
    """ Base class for objects that contain a set of properties.
    """

    def __init__(self, **properties):
        self.__properties = PropertySet(properties or {})

    def __eq__(self, other):
        return self.properties == other.properties

    def __ne__(self, other):
        return not self.__eq__(other)

    def __getitem__(self, key):
        return self.properties.__getitem__(key)

    def __setitem__(self, key, value):
        return self.properties.__setitem__(key, value)

    def __delitem__(self, key):
        return self.properties.__delitem__(key)

    @property
    def properties(self):
        """ The set of properties attached to this object.
        """
        return self.__properties

    def replace(self, **properties):
        """ Replace the properties with those provided.
        """
        self.properties.clear()
        self.properties.update(properties)


class Node(Entity, PropertyContainer, yaml.YAMLObject):
    """ A local representation of a Neo4j graph node that may be bound to a
    node in a remote graph database.
    """
    yaml_tag = "!Node"

    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        labels = mapping.get("labels") or []
        properties = mapping.get("properties") or {}
        inst = cls(*labels, **properties)
        id_ = mapping.get("id")
        if id_ is None:
            return inst
        else:
            return loader.__graph__.bind_node(id_, inst)

    @classmethod
    def cast(cls, x):
        if x is None:
            return None
        if isinstance(x, cls):
            return x
        if not isinstance(x, (tuple, list, set)):
            x = [x]
        labels = []
        properties = {}
        for item in x:
            if isinstance(item, dict):
                properties.update(item)
            else:
                labels.append(item)
        return cls(*labels, **properties)
    
    def __init__(self, *labels, **properties):
        Entity.__init__(self)
        PropertyContainer.__init__(self, **properties)
        self.__labels = set(labels)

    def __repr__(self):
        return self.to_geoff()

    def __eq__(self, other):
        return (self.labels == other.labels and
                PropertyContainer.__eq__(self, other))

    @property
    def cypher_id(self):
        """ A unique ID used in Cypher queries.
        """
        if self.bound:
            return "BN{0}".format(self._id)
        else:
            return "N{0}".format(id(self))

    @property
    def labels(self):
        """ The set of text labels applied to this Node.
        """
        return self.__labels

    def replace(self, *labels, **properties):
        """ Replace the labels and properties on this ``Node`` with those
        provided.
        """
        self.__labels = set(labels)
        PropertyContainer.replace(self, **properties)

    @property
    def exists(self):
        """ Returns :const:`True` if the entity to which this is bound exists
        in the remote graph database.
        """
        self.assert_bound()
        try:
            Batch.single(self.graph, Batch.get_node, self._id)
        except Error:  # TODO: NotExistsError (V1)
            return False
        else:
            return True

    def pull(self):
        """ Update local node from remote node.
        """
        self.assert_bound()
        remote = Batch.single(self.graph, Batch.get_node, self._id)
        self.replace(*remote.labels, **remote.properties)

    def push(self):
        """ Update remote node from local node.
        """
        self.assert_bound()
        Batch.single(self.graph, Batch.set_node, self._id,
                     self.__labels, self.properties)

    def to_cypher(self, **kwargs):
        """ Return a Cypher representation of this node.
        """
        s = [self.cypher_id]
        if kwargs.get("labels"):
            for label in sorted(self.__labels):
                s.append(":")
                s.append(label)
        if kwargs.get("properties"):
            if self.properties:
                if s:
                    s.append(" ")
                s.append(self.properties.to_cypher(**kwargs))
        s = ["("] + s + [")"]
        return "".join(s)

    def to_geoff(self):
        """ Return a Geoff representation of this node.
        """
        s = []
        if self.bound:
            s.append(str(self._id))
        for label in sorted(self.__labels):
            s.append(":")
            s.append(label)
        if self.properties:
            if s:
                s.append(" ")
            s.append(self.properties.to_json())
        s = ["("] + s + [")"]
        return "".join(s)


class Rel(Entity, PropertyContainer, yaml.YAMLObject):
    """ A local representation of the type and properties of a Neo4j graph
    relationship that may be bound to a relationship in a remote graph
    database. A ``Rel`` does not hold information on its start or end nodes,
    a :class:`Relationship` is used for that instead.
    """
    yaml_tag = "!Rel"
    
    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        type_ = mapping.get("type")
        properties = mapping.get("properties") or {}
        inst = cls(type_, **properties)
        id_ = mapping.get("id")
        if id_ is None:
            return inst
        else:
            return loader.__graph__.bind_rel(id_, inst)

    @classmethod
    def cast(cls, x):
        if x is None:
            return None
        if isinstance(x, cls):
            return x
        if not isinstance(x, (tuple, list, set)):
            x = [x]
        type_ = []
        properties = {}
        for item in x:
            if isinstance(item, dict):
                properties.update(item)
            else:
                type_.append(item)
        return cls(*type_, **properties)
    
    def __init__(self, *type_, **properties):
        Entity.__init__(self)
        PropertyContainer.__init__(self, **properties)
        if len(type_) == 0:
            raise ValueError("A relationship type is required")
        elif len(type_) > 1:
            raise ValueError("Only one relationship type can be specified")
        self.__type = type_[0]
        self.__reverse = False

    def __repr__(self):
        return self.to_geoff()

    def __eq__(self, other):
        return (self.type == other.type and
                PropertyContainer.__eq__(self, other))

    @property
    def cypher_id(self):
        """ A unique ID used in Cypher queries.
        """
        if self.bound:
            return "BR{0}".format(self._id)
        else:
            return "R{0}".format(id(self))

    @property
    def type(self):
        return self.__type

    @type.setter
    def type(self, name):
        self.__type = name

    def replace(self, *type_, **properties):
        """ Replace the properties on this ``Rel`` with those provided.
        """
        if len(type_) == 0:
            raise ValueError("A relationship type is required")
        elif len(type_) > 1:
            raise ValueError("Only one relationship type can be specified")
        self.__type = type_[0]
        PropertyContainer.replace(self, **properties)

    @property
    def exists(self):
        self.assert_bound()
        try:
            Batch.single(self.graph, Batch.get_rel, self._id)
        except Error:  # TODO: NotExistsError (V1)
            return False
        else:
            return True

    def pull(self):
        self.assert_bound()
        remote_path = Batch.single(self.graph, Batch.get_rel,
                                   self._id)
        remote_rel = remote_path.rels[0]
        self.replace(remote_rel.type, **remote_rel.properties)

    def push(self):
        self.assert_bound()
        Batch.single(self.graph, Batch.set_rel, self._id,
                     self.properties)

    def to_cypher(self, **kwargs):
        s = [self.cypher_id]
        if kwargs.get("type", not self.bound):
            s.append(":")
            s.append(self.__type)
        if kwargs.get("properties"):
            if self.properties:
                s.append(" ")
                s.append(self.properties.to_cypher(**kwargs))
        if self.__reverse:
            s = ["<-["] + s + ["]-"]
        else:
            s = ["-["] + s + ["]->"]
        return "".join(s)

    def to_geoff(self):
        s = []
        if self.bound:
            s.append(str(self._id))
        s.append(":")
        s.append(self.__type)
        if self.properties:
            s.append(" ")
            s.append(self.properties.to_json())
        if self.__reverse:
            s = ["<-["] + s + ["]-"]
        else:
            s = ["-["] + s + ["]->"]
        return "".join(s)


class Rev(Rel):
    yaml_tag = "!Rev"

    def __init__(self, *type_, **properties):
        Rel.__init__(self, *type_, **properties)
        self._Rel__reverse = True


class Path(Bindable, yaml.YAMLObject):
    """ An alternating chain of :class:`Node` and :class:`Rel` objects.
    """
    yaml_tag = '!Path'

    @classmethod
    def from_yaml(cls, loader, node):
        sequence = loader.construct_sequence(node, deep=True)
        count = len(sequence)
        if count == 3:
            if isinstance(sequence[1], Rev):
                return Relationship(*reversed(sequence))
            else:
                return Relationship(*sequence)
        else:
            return Path(*sequence)

    def __init__(self, node, *rels_and_nodes):
        Bindable.__init__(self)
        if len(rels_and_nodes) % 2 != 0:
            raise ValueError("An even number of trailing rels and nodes must "
                             "be provided")
        self.__nodes = (Node.cast(node),)
        self.__nodes += tuple(map(Node.cast, rels_and_nodes[1::2]))
        self.__rels = tuple(map(Rel.cast, rels_and_nodes[0::2]))
        # Derive bindings (if any).
        graphs = set()
        for entity in round_robin(self.__nodes, self.__rels):
            if entity.bound:
                graphs.add(entity.graph)
        # Check everything belongs to the same graph (if any).
        if len(graphs) > 1:
            raise ValueError("Bound path entities cannot span multiple "
                             "graphs")
        # If all is valid, assign attributes.
        try:
            Bindable.bind(self, graphs.pop())
        except KeyError:
            Bindable.bind(self, None)

    def __repr__(self):
        return self.to_geoff()

    def __eq__(self, other):
        return self.nodes == other.nodes and self.rels == other.rels

    def __getitem__(self, index):
        return self.relationship(index)

    def __iter__(self):
        for i, rel in enumerate(self.__rels):
            yield Relationship(self.__nodes[i], rel, self.__nodes[i + 1])

    def __len__(self):
        return self.size

    def __reversed__(self):
        # TODO (V1)
        pass

    @property
    def order(self):
        return len(self.__nodes)

    @property
    def size(self):
        return len(self.__rels)

    @property
    def start_node(self):
        """ The first :class:`Node` in this path.
        """
        return self.__nodes[0]

    @property
    def end_node(self):
        """ The last :class:`Node` in this path.
        """
        return self.__nodes[-1]

    @property
    def nodes(self):
        """ Tuple of all ``Nodes`` in this path.
        """
        return self.__nodes

    @property
    def rels(self):
        """ Tuple of all ``Rels`` in this path.
        """
        return self.__rels

    def relationship(self, index):
        try:
            return Relationship(self.__nodes[index], self.__rels[index],
                                self.__nodes[index + 1])
        except IndexError:
            raise IndexError("Index out of range")

    def bind(self, graph, **kwargs):
        raise TypeError("Cannot directly bind a path")  # TODO - change error type

    def unbind(self):
        raise TypeError("Cannot directly unbind a path")  # TODO - change error type

    def pull(self):
        self.assert_bound()
        batch = BatchPull(self.graph)
        batch.add(self)
        batch.submit()

    def push(self):
        self.assert_bound()
        batch = BatchPush(self.graph)
        batch.add(self)
        batch.submit()

    def to_cypher(self, **kwargs):
        s = [self.__nodes[0].to_cypher(**kwargs)]
        for i, rel in enumerate(self.__rels):
            s.append(rel.to_cypher(**kwargs))
            s.append(self.__nodes[i + 1].to_cypher(**kwargs))
        return "".join(s)

    def to_geoff(self):
        s = [self.__nodes[0].to_geoff()]
        for i, rel in enumerate(self.__rels):
            s.append(rel.to_geoff())
            s.append(self.__nodes[i + 1].to_geoff())
        return "".join(s)


class Relationship(Path):
    """ A :class:`Path` segment consisting of two :class:`Node` objects and one
    :class:`Rel` object; this is analogous to a Neo4j Relationship.
    """

    def __init__(self, start_node, rel, end_node):
        Path.__init__(self, start_node, rel, end_node)
        self.__rel = self.rels[0]

    def __getitem__(self, key):
        return self.__rel.properties.__getitem__(key)

    def __setitem__(self, key, value):
        return self.__rel.properties.__setitem__(key, value)

    def __delitem__(self, key):
        return self.__rel.properties.__delitem__(key)

    @property
    def bound(self):
        return self.__rel.bound

    @property
    def cypher_id(self):
        """ A unique ID used in Cypher queries.
        """
        return self.__rel.cypher_id

    @property
    def _id(self):
        return self.__rel._id

    @property
    def type(self):
        return self.__rel.type

    @property
    def properties(self):
        return self.__rel.properties

    def replace(self, **properties):
        """ Replace the properties on this Relationship with those provided.
        """
        self.__rel.replace(**properties)

    @property
    def exists(self):
        return self.__rel.exists


class BatchPull(object):

    def __init__(self, graph):
        self.__graph = graph
        self.__nodes = {}
        self.__rels = {}

    def __assert_familiar(self, entity):
        if entity.graph != self.__graph:
            raise ValueError("Entities are from different graphs")

    def add(self, entity):
        """ Add an entity to the set of entities to be pulled.
        """
        assert_bound(entity)
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            self.__nodes.setdefault(entity._id, []).append(entity)
        elif isinstance(entity, Rel):
            self.__rels.setdefault(entity._id, []).append(entity)
        elif isinstance(entity, Path):
            for node in entity.nodes:
                self.__nodes.setdefault(node._id, []).append(entity)
            for rel in entity.rels:
                self.__rels.setdefault(rel._id, []).append(entity)
        else:
            raise TypeError("Unexpected entity type")

    def remove(self, entity):
        """ Remove an entity from the set of entities to be pulled.
        """
        assert_bound(entity)
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            self.__nodes[entity._id].remove(entity)
        elif isinstance(entity, Rel):
            self.__rels[entity._id].remove(entity)
        elif isinstance(entity, Path):
            for node in entity.nodes:
                try:
                    self.__nodes[node._id].remove(entity)
                except ValueError:
                    pass
            for rel in entity.rels:
                try:
                    self.__rels[rel._id].remove(entity)
                except ValueError:
                    pass
        else:
            raise TypeError("Unexpected entity type")

    def submit(self):
        """ Submit the batch to update all local entities.
        """
        batch = Batch(self.__graph)
        for id_ in self.__nodes.keys():
            batch.get_node(id_)
        for id_ in self.__rels.keys():
            batch.get_rel(id_)
        results = batch.submit()
        for entity in results:
            if isinstance(entity, Node):
                id_ = entity._id
                for node_or_path in self.__nodes[id_]:
                    if isinstance(node_or_path, Node):
                        node_or_path.replace(*entity.labels, **entity.properties)
                    elif isinstance(node_or_path, Path):
                        for node in node_or_path.nodes:
                            if node._id == id_:
                                node.replace(*entity.labels, **entity.properties)
            elif isinstance(entity, Path):
                path_rel = entity.rels[0]
                id_ = path_rel._id
                for rel_or_path in self.__rels[id_]:
                    if isinstance(rel_or_path, Rel):
                        rel_or_path.replace(path_rel.type, **path_rel.properties)
                    elif isinstance(rel_or_path, Path):
                        for rel in rel_or_path.rels:
                            if rel._id == id_:
                                rel.replace(path_rel.type, **path_rel.properties)
            else:
                raise TypeError("Unexpected entity type")


class BatchPush(object):

    def __init__(self, graph):
        self.__graph = graph
        self.__nodes = {}
        self.__rels = {}

    def __assert_familiar(self, entity):
        if entity.graph != self.__graph:
            raise ValueError("Entities are from different graphs")

    def add(self, entity):
        """ Add an entity to the set of entities to be pushed.
        """
        assert_bound(entity)
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            self.__nodes[entity._id] = entity
        elif isinstance(entity, Rel):
            self.__rels[entity._id] = entity
        elif isinstance(entity, Path):
            for node in entity.nodes:
                self.__nodes[node._id] = node
            for rel in entity.rels:
                self.__rels[rel._id] = rel
        else:
            raise TypeError("Unexpected entity type")

    def remove(self, entity):
        """ Remove an entity from the set of entities to be pushed.
        """
        assert_bound(entity)
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            del self.__nodes[entity._id]
        elif isinstance(entity, Rel):
            del self.__rels[entity._id]
        elif isinstance(entity, Path):
            for node in entity.nodes:
                try:
                    del self.__nodes[node._id]
                except KeyError:
                    pass
            for rel in entity.rels:
                try:
                    del self.__rels[rel._id]
                except KeyError:
                    pass
        else:
            raise TypeError("Unexpected entity type")

    def submit(self):
        """ Submit the batch to update all remote entities.
        """
        batch = Batch(self.__graph)
        for id_, node in self.__nodes.items():
            batch.set_node(id_, node.labels, node.properties)
        for id_, rel in self.__rels.items():
            batch.set_rel(id_, rel.properties)
        batch.submit()


class BatchCreate(object):

    def __init__(self, graph):
        self.__graph = graph
        self.__nodes = []
        self.__paths = []

    def __assert_familiar(self, entity):
        if entity.graph is not None and entity.graph != self.__graph:
            raise ValueError("Entities are from different graphs")

    def add(self, entity):
        """ Add an entity to the set of entities to be created.
        """
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            self.__nodes.append(entity)
        elif isinstance(entity, Path):
            self.__paths.append(entity)
        else:
            raise TypeError("Unexpected entity type")

    def remove(self, entity):
        """ Remove an entity from the set of entities to be created.
        """
        self.__assert_familiar(entity)
        if isinstance(entity, Node):
            self.__nodes.remove(entity)
        elif isinstance(entity, Path):
            self.__paths.remove(entity)
        else:
            raise TypeError("Unexpected entity type")

    @staticmethod
    def __create_node_as_path(node):
        clauses = []
        params = {}
        cypher_id = node.cypher_id
        param_id = cypher_id + "P"
        if node.bound:
            clauses.append("START {0}=node({1})".format(cypher_id, node._id))
            clauses.append("MATCH p=({0})".format(cypher_id))
        else:
            clauses.append("CREATE p=({0})".format(cypher_id))
        if node.labels:
            clauses.append("SET {0}:{1}".format(cypher_id, ":".join(node.labels)))
        if node.properties:
            clauses.append("SET {0}={{{1}}}".format(cypher_id, param_id))
            params[param_id] = node.properties
        clauses.append("RETURN p")
        return " ".join(clauses), params

    @staticmethod
    def __create_relationship_as_path(relationship):
        # TODO: refactor this monstrosity
        clauses = []
        params = {}

        start_node = relationship.start_node
        end_node = relationship.end_node
        cypher_id = relationship.cypher_id
        param_id = cypher_id + "P"

        if relationship.bound:
            clauses.append("START {0}=rel({1})".format(cypher_id, relationship._id))
            clauses.append("MATCH p=({0})-[{1}]->({2})".format(start_node.cypher_id, cypher_id, end_node.cypher_id))
        elif start_node.bound and end_node.bound:
            clauses.append("START {0}=node({1}),{2}=node({3})".format(start_node.cypher_id, start_node._id, end_node.cypher_id, end_node._id))
            clauses.append("CREATE p=({0})-[:`{1}`]->({2})".format(start_node.cypher_id, relationship.type.replace("`", "``"), end_node.cypher_id))
        elif start_node.bound:
            clauses.append("START {0}=node({1})".format(start_node.cypher_id, start_node._id))
            clauses.append("CREATE p=({0})-[:`{1}`]->({2})".format(start_node.cypher_id, relationship.type.replace("`", "``"), end_node.cypher_id))
        elif end_node.bound:
            clauses.append("START {0}=node({1})".format(end_node.cypher_id, end_node._id))
            clauses.append("CREATE p=({0})-[:`{1}`]->({2})".format(start_node.cypher_id, relationship.type.replace("`", "``"), end_node.cypher_id))
        else:
            clauses.append("CREATE p=({0})-[:`{1}`]->({2})".format(start_node.cypher_id, relationship.type.replace("`", "``"), end_node.cypher_id))

        if start_node.labels:
            clauses.append("SET {0}:{1}".format(start_node.cypher_id, ":".join(start_node.labels)))
        if start_node.properties:
            clauses.append("SET {0}={{{0}P}}".format(start_node.cypher_id, param_id))
            params["{0}P".format(start_node.cypher_id)] = start_node.properties

        if end_node.labels:
            clauses.append("SET {0}:{1}".format(end_node.cypher_id, ":".join(end_node.labels)))
        if end_node.properties:
            clauses.append("SET {0}={{{0}P}}".format(end_node.cypher_id, param_id))
            params["{0}P".format(end_node.cypher_id)] = end_node.properties

        if relationship.properties:
            clauses.append("SET {0}={{{1}}}".format(cypher_id, param_id))
            params[param_id] = relationship.properties

        clauses.append("RETURN p")
        return " ".join(clauses), params

    def submit(self):
        """ Submit the batch to create or update the remote entities.
        """
        created = []
        batch = Batch(self.__graph)
        for node in self.__nodes:
            created.append(node)
            if node.bound:
                batch.set_node(node._id, node.labels, node.properties)
            else:
                batch.create_node(node.labels, node.properties)
        for path in self.__paths:
            created.append(path)
            if len(path) == 0:
                query, params = self.__create_node_as_path(path.start_node)
                batch.execute_cypher(query, params)
            elif len(path) == 1:
                query, params = self.__create_relationship_as_path(path.relationship(0))
                batch.execute_cypher(query, params)
            else:
                raise ValueError("Long paths not yet supported")
        for i, result in enumerate(batch.submit()):
            if isinstance(result, Table):
                path = result[0][0]
                for j, node in enumerate(path.nodes):
                    created[i].nodes[j].bind(self.__graph, id=node._id)
                    created[i].nodes[j].replace(*node.labels, **node.properties)
                for j, rel in enumerate(path.rels):
                    created[i].rels[j].bind(self.__graph, id=rel._id)
                    created[i].rels[j].replace(rel.type, **rel.properties)
            else:
                node = result
                created[i].bind(self.__graph, id=node._id)
                created[i].replace(*node.labels, **node.properties)
        return created


# Patch serialisable classes for the CLoader
for cls in (Graph, Node, Rel, Rev, Path):
    Loader.add_constructor(cls.yaml_tag, cls.from_yaml)
