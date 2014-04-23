#!/usr/bin/env python

import json
import logging
import yaml
import zmq


log = logging.getLogger(__name__)
log.addHandler(logging.NullHandler())


GET = "GET"
SET = "SET"
PATCH = "PATCH"
CREATE = "CREATE"
DELETE = "DELETE"
EXECUTE = "EXECUTE"


def is_safe_char(x):
    return x.isalpha() or x.isdigit() or x == "_"


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

        class GraphLoader(yaml.Loader):
            __graph__ = graph

        full = ""
        more = True
        while more:
            try:
                frame = graph.socket.recv(copy=False)
            except zmq.error.ZMQError as err:
                raise TimeoutError("Timeout occurred while trying to receive "
                                   "data")
            else:
                full += frame.bytes.decode("utf-8")
                more = frame.more
        if full:
            for document in yaml.load_all(full, Loader=GraphLoader):
                yield Response(document or {})
        else:
            yield Response({})

    def __init__(self, document):
        self.__head = document.get("head")
        self.__body = document.get("body")
        self.__foot = document.get("foot")
        if "error" in document:
            raise Error(document["error"])

    def __repr__(self):
        s = ["Response"]
        if self.__head:
            s.append("head={0}".format(repr(self.__head)))
        if self.__body:
            s.append("body={0}".format(repr(self.__body)))
        if self.__foot:
            s.append("foot={0}".format(repr(self.__foot)))
        return "<" + " ".join(s) + ">"

    @property
    def head(self):
        return self.__head

    @property
    def body(self):
        return self.__body

    @property
    def foot(self):
        return self.__foot

    def to_table(self):
        return Table(self.__head["columns"], self.__body)


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
        self.__socket.send(b"")  # to close multipart message
        for result in Response.receive(self.__graph):
            # interpret the result type (should this be explicit?)
            if isinstance(result.body, list):
                if result.head and "columns" in result.head:
                    value = result.to_table()
                else:
                    value = iter(result.body)
            else:
                value = result.body
            yield value
        self.__count = 0

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
    yaml_tag = '!Graph'

    ZEROGRAPH_PORT = 47470

    __services = {}

    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        host = mapping.get("host")
        port = mapping.get("port")
        inst = cls(host=host, port=port)
        return inst

    @classmethod
    def open(cls, host="localhost", port=ZEROGRAPH_PORT):
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
        return self.__host

    @property
    def port(self):
        return self.__port

    @property
    def address(self):
        return self.__address

    @property
    def socket(self):
        return self.__socket

    @property
    def zerograph(self):
        if self.__port == self.ZEROGRAPH_PORT:
            return self
        else:
            return self.open(self.__host)

    @property
    def order(self):
        # TODO: count all nodes
        return None

    @property
    def size(self):
        # TODO: count all rels
        return None

    def drop(self):
        if self.__port == self.ZEROGRAPH_PORT:
            raise ValueError("Cannot drop zerograph")
        else:
            zerograph = Graph.open(self.__host)
            return Batch.single(zerograph, Batch.delete_graph, self.__host,
                                self.__port)
        # TODO: mark as dropped and disallow any further actions? (maybe)

    def batch(self):
        return Batch(self)

    def execute(self, query, *param_sets):
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

    def node(self, node_id):
        return Batch.single(self, Batch.get_node, node_id)

    def path(self, rel_id):
        return Batch.single(self, Batch.get_rel, rel_id)

    def create(self, *entities):
        batch = Batch(self)
        for entity in entities:
            if isinstance(entity, Node):
                batch.create_node(entity.labels, entity.properties)
            elif isinstance(entity, Path):
                pass  # TODO
            else:
                raise ValueError("Cannot create a "
                                 "{}".format(entity.__class__.__name__))
        return batch.submit()

    def delete(self, *entities):
        # TODO
        pass

    def find(self, label, key=None, value=None):
        return Batch.single(self, Batch.get_node_set, label, key, value)


class Bindable(object):
    """ Mixin for objects that can be bound to remote graph database entities.
    """

    def __init__(self):
        self.__graph = None
        self.__id = None

    @property
    def bound_graph(self):
        return self.__graph

    @property
    def bound_id(self):
        return self.__id

    @property
    def bound(self):
        return self.__graph is not None and self.__id is not None

    def bind(self, graph, id):
        self.__graph = graph
        self.__id = id

    def unbind(self):
        self.__graph = None
        self.__id = None

    def __assert_bound(self):
        if not self.bound:
            raise UnboundError(self)

    def pull(self):
        self.__assert_bound()

    def push(self):
        self.__assert_bound()

    def delete(self):
        self.__assert_bound()


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

    def to_cypher(self):
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
    """ Base class for entities that contain properties.
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
        return self.__properties


class Node(Bindable, PropertyContainer, yaml.YAMLObject):
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
        if id_ is not None:
            inst.bind(loader.__graph__, id_)
        return inst

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
        Bindable.__init__(self)
        PropertyContainer.__init__(self, **properties)
        self.__labels = set(labels or [])

    def __repr__(self):
        return self.to_cypher()

    def __eq__(self, other):
        return (PropertyContainer.__eq__(self, other) and
                self.labels == other.labels)

    def __ne__(self, other):
        return not self.__eq__(other)

    @property
    def labels(self):
        return self.__labels

    @property
    def exists(self):
        Bindable.pull(self)
        try:
            Batch.single(self.bound_graph, Batch.get_node, self.bound_id)
        except Error:  # TODO: NotExistsError
            return False
        else:
            return True

    def pull(self):
        Bindable.pull(self)
        remote = Batch.single(self.bound_graph, Batch.get_node, self.bound_id)
        self.__labels = set(remote.labels)
        self.properties.clear()
        self.properties.update(remote.properties)

    def push(self):
        Bindable.push(self)
        Batch.single(self.bound_graph, Batch.set_node, self.bound_id,
                     self.__labels, self.properties)

    def delete(self):
        Bindable.delete(self)
        Batch.single(self.bound_graph, Batch.delete_node, self.bound_id)

    def to_cypher(self):
        s = []
        if self.bound:
            s.append("_")
            s.append(str(self.bound_id))
        for label in sorted(self.__labels):
            s.append(":")
            s.append(label)
        if self.properties:
            if s:
                s.append(" ")
            s.append(self.properties.to_cypher())
        s = ["("] + s + [")"]
        return "".join(s)

    def to_geoff(self):
        s = []
        if self.bound:
            s.append(str(self.bound_id))
        for label in sorted(self.__labels):
            s.append(":")
            s.append(label)
        if self.properties:
            if s:
                s.append(" ")
            s.append(self.properties.to_json())
        s = ["("] + s + [")"]
        return "".join(s)


class Rel(Bindable, PropertyContainer, yaml.YAMLObject):
    yaml_tag = "!Rel"
    
    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        type_ = mapping.get("type")
        properties = mapping.get("properties") or {}
        inst = cls(type_, **properties)
        id_ = mapping.get("id")
        if id_ is not None:
            inst.bind(loader.__graph__, id_)
        return inst

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
        Bindable.__init__(self)
        PropertyContainer.__init__(self, **properties)
        if len(type_) == 0:
            raise ValueError("A relationship type is required")
        elif len(type_) > 1:
            raise ValueError("Only one relationship type can be specified")
        self.__type = type_[0]
        self.__reverse = False

    def __repr__(self):
        return self.to_cypher()

    @property
    def type(self):
        return self.__type

    @property
    def reverse(self):
        return self.__reverse

    @property
    def exists(self):
        Bindable.pull(self)
        try:
            Batch.single(self.bound_graph, Batch.get_rel, self.bound_id)
        except Error:  # TODO: NotExistsError
            return False
        else:
            return True

    def pull(self):
        Bindable.pull(self)
        remote_path = Batch.single(self.bound_graph, Batch.get_rel,
                                   self.bound_id)
        remote_rel = remote_path.rels[0]
        self.__type = remote_rel.type
        self.properties.clear()
        self.properties.update(remote_rel.properties)

    def push(self):
        Bindable.push(self)
        Batch.single(self.bound_graph, Batch.set_rel, self.bound_id,
                     self.properties)

    def delete(self):
        Bindable.delete(self)
        Batch.single(self.bound_graph, Batch.delete_rel, self.bound_id)

    def to_cypher(self):
        s = []
        if self.bound:
            s.append(str(self.bound_id))
        s.append(":")
        s.append(self.__type)
        if self.properties:
            s.append(" ")
            s.append(self.properties.to_cypher())
        if self.__reverse:
            s = ["<-["] + s + ["]-"]
        else:
            s = ["-["] + s + ["]->"]
        return "".join(s)

    def to_geoff(self):
        s = []
        if self.bound:
            s.append(str(self.bound_id))
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


class Path(yaml.YAMLObject):
    yaml_tag = '!Path'

    @classmethod
    def from_yaml(cls, loader, node):
        """ Hydrate Path from YAML.
        
        !Path [!Node {}, !Rel {"type":"KNOWS"}, !Node {}]
        
        """
        sequence = loader.construct_sequence(node, deep=True)
        inst = cls(*sequence)
        return inst
            
    def __init__(self, node, *rels_and_nodes):
        if len(rels_and_nodes) % 2 != 0:
            raise ValueError("An even number of trailing rels and nodes must "
                             "be provided")
        self.__nodes = (Node.cast(node),)
        self.__nodes += tuple(map(Node.cast, rels_and_nodes[1::2]))
        self.__rels = tuple(map(Rel.cast, rels_and_nodes[0::2]))
        graphs = ({n.bound_graph for n in self.__nodes if n.bound_graph} |
                  {r.bound_graph for r in self.__rels if r.bound_graph})
        if len(graphs) > 1:
            raise ValueError("Bound path entities cannot span multiple "
                             "graphs")

    def __repr__(self):
        return self.to_cypher()

    def __getitem__(self, index):
        try:
            return Path(self.__nodes[index], self.__rels[index],
                        self.__nodes[index + 1])
        except IndexError:
            raise IndexError("Path segment index out of range")

    def __iter__(self):
        for i, rel in enumerate(self.__rels):
            yield Path(self.__nodes[i], rel, self.__nodes[i + 1])

    def __len__(self):
        return self.size

    def __reversed__(self):
        # TODO
        pass

    @property
    def order(self):
        return len(self.__nodes)

    @property
    def size(self):
        return len(self.__rels)

    @property
    def start_node(self):
        return self.__nodes[0]

    @property
    def end_node(self):
        return self.__nodes[-1]

    @property
    def nodes(self):
        return self.__nodes

    @property
    def rels(self):
        return self.__rels

    def pull(self):
        # TODO - get_path
        pass

    def push(self):
        # TODO - set_path
        pass
    
    def delete(self):
        # TODO - delete_path
        pass

    def to_cypher(self):
        s = [self.__nodes[0].to_cypher()]
        for i, rel in enumerate(self.__rels):
            s.append(rel.to_cypher())
            s.append(self.__nodes[i + 1].to_cypher())
        return "".join(s)

    def to_geoff(self):
        s = [self.__nodes[0].to_geoff()]
        for i, rel in enumerate(self.__rels):
            s.append(rel.to_geoff())
            s.append(self.__nodes[i + 1].to_geoff())
        return "".join(s)
