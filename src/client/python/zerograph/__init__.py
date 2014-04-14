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


class NotLinkedError(Exception):
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
        line = " ".join((self.__method, self.__resource, json.dumps(self.__arguments, separators=",:", ensure_ascii=True, cls=ZerographEncoder)))
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
                yield Response(document)
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
        out = [" " + " | ".join(column.ljust(column_widths[i])
                                for i, column in enumerate(self.__columns)) + " "]
        out += ["-" + "-+-".join("-" * column_widths[i]
                                 for i, column in enumerate(self.__columns)) + "-"]
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
        Request(method, resource, **args).send(self.__socket, more=True)
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

    def get_graph(self, host, port):
        return self.append(GET, "Graph", host=host, port=int(port))

    def open_graph(self, host, port):
        return self.append(SET, "Graph", host=host, port=int(port))

    def drop_graph(self, host, port):
        return self.append(DELETE, "Graph", host=host, port=int(port))

    def execute(self, query, *param_sets):
        param_set_count = len(param_sets)
        if param_set_count == 0:
            return self.append(EXECUTE, "Cypher", query=query)
        elif param_set_count == 1:
            return self.append(EXECUTE, "Cypher", query=query, params=dict(param_sets[0]))
        else:
            pointers = []
            for param_set in param_sets:
                pointers.append(self.append(EXECUTE, "Cypher", query=query, params=dict(param_set)))
            return pointers

    def get_node(self, node_id):
        return self.append(GET, "Node", id=int(node_id))

    def set_node(self, node_id, labels, properties):
        return self.append(SET, "Node", id=int(node_id), labels=labels, properties=properties)

    def patch_node(self, node_id, labels, properties):
        return self.append(PATCH, "Node", id=int(node_id), labels=labels, properties=properties)

    def create_node(self, labels=None, properties=None):
        return self.append(CREATE, "Node", labels=list(labels or []), properties=dict(properties or {}))

    def delete_node(self, node_id):
        return self.append(DELETE, "Node", id=int(node_id))

    def get_rel(self, rel_id):
        return self.append(GET, "Rel", id=int(rel_id))

    def set_rel(self, rel_id, properties):
        return self.append(SET, "Rel", id=int(rel_id), properties=properties)

    def patch_rel(self, rel_id, properties):
        return self.append(PATCH, "Rel", id=int(rel_id), properties=properties)

    def create_rel(self, start_node, type, end_node, properties=None):
        if isinstance(start_node, Node):
            if start_node.linked_graph == self.__graph:
                start_node = start_node.linked_id
            else:
                raise ValueError("Start node belongs to a different graph")
        if isinstance(end_node, Node):
            if end_node.linked_graph == self.__graph:
                end_node = end_node.linked_id
            else:
                raise ValueError("End node belongs to a different graph")
        return self.append(CREATE, "Rel", start=start_node, end=end_node, type=type, properties=dict(properties or {}))

    def delete_rel(self, rel_id):
        return self.append(DELETE, "Rel", id=int(rel_id))

    def match_nodes(self, label, key=None, value=None):
        if key is None:
            return self.append(GET, "NodeSet", label=label)
        else:
            return self.append(GET, "NodeSet", label=label, key=key, value=value)

    def merge_nodes(self, label, key, value):
        return self.append(PATCH, "NodeSet", label=label, key=key, value=value)

    def purge_nodes(self, label, key, value):
        return self.append(DELETE, "NodeSet", label=label, key=key, value=value)

    def match_rels(self, start_node=None, type=None, end_node=None):
        if isinstance(start_node, Node):
            start_node = start_node.linked_id
        elif start_node is not None:
            raise TypeError("Start node must be a Node instance")
        if isinstance(end_node, Node):
            end_node = end_node.linked_id
        elif end_node is not None:
            raise TypeError("End node must be a Node instance")
        return self.append(GET, "RelSet", start=start_node, end=end_node, type=type)

    def merge_rels(self, start_node, type, end_node):
        if isinstance(start_node, Node):
            start_node = start_node.linked_id
        else:
            raise TypeError("Start node must be a Node instance")
        if isinstance(end_node, Node):
            end_node = end_node.linked_id
        else:
            raise TypeError("End node must be a Node instance")
        return self.append(PATCH, "RelSet", start=start_node, end=end_node, type=type)

    def purge_rels(self, start_node=None, type=None, end_node=None):
        if isinstance(start_node, Node):
            start_node = start_node.linked_id
        elif start_node is not None:
            raise TypeError("Start node must be a Node instance")
        if isinstance(end_node, Node):
            end_node = end_node.linked_id
        elif end_node is not None:
            raise TypeError("End node must be a Node instance")
        return self.append(DELETE, "RelSet", start=start_node, end=end_node, type=type)


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
                graph = Batch.single(zerograph, Batch.open_graph, zerograph.host, port)
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

    def drop(self):
        if self.__port == self.ZEROGRAPH_PORT:
            raise ValueError("Cannot drop zerograph")
        else:
            zerograph = Graph.open(self.__host)
            return Batch.single(zerograph, Batch.drop_graph, self.__host, self.__port)
        # TODO: mark as dropped and disallow any further actions? (maybe)

    def create_batch(self):
        return Batch(self)

    def execute(self, query, *param_sets):
        param_set_count = len(param_sets)
        if param_set_count == 0:
            return Batch.single(self, Batch.execute, query)
        elif param_set_count == 1:
            return Batch.single(self, Batch.execute, query, param_sets[0])
        else:
            batch = Batch(self)
            for param_set in param_sets:
                Batch.execute(batch, query, param_set)
            results = batch.submit()
            return list(results)

    def get_node(self, node_id):
        return Batch.single(self, Batch.get_node, node_id)

    def set_node(self, node_id, labels, properties):
        return Batch.single(self, Batch.set_node, node_id, labels, properties)

    def patch_node(self, node_id, labels, properties):
        return Batch.single(self, Batch.patch_node, node_id, labels, properties)

    def create_node(self, labels=None, properties=None):
        return Batch.single(self, Batch.create_node, labels, properties)

    def delete_node(self, node_id):
        return Batch.single(self, Batch.delete_node, node_id)

    def get_rel(self, rel_id):
        return Batch.single(self, Batch.get_rel, rel_id)

    def set_rel(self, rel_id, properties):
        return Batch.single(self, Batch.set_rel, rel_id, properties)

    def patch_rel(self, rel_id, properties):
        return Batch.single(self, Batch.patch_rel, rel_id, properties)

    def create_rel(self, start_node, type, end_node, properties=None):
        return Batch.single(self, Batch.create_rel, start_node, type, end_node, properties)

    def delete_rel(self, rel_id):
        return Batch.single(self, Batch.delete_rel, rel_id)

    def match_nodes(self, label, key=None, value=None):
        return Batch.single(self, Batch.match_nodes, label, key, value)

    def merge_nodes(self, label, key, value):
        return Batch.single(self, Batch.merge_nodes, label, key, value)

    def purge_nodes(self, label, key, value):
        return Batch.single(self, Batch.purge_nodes, label, key, value)

    def match_rels(self, start_node=None, type=None, end_node=None):
        return Batch.single(self, Batch.match_rels, start_node, type, end_node)

    def merge_rels(self, start_node, type, end_node):
        return Batch.single(self, Batch.merge_rels, start_node, type, end_node)

    def purge_rels(self, start_node=None, type=None, end_node=None):
        return Batch.single(self, Batch.purge_rels, start_node, type, end_node)


class Linkable(object):
    """ Mixin for objects that can be linked to remote graph database entities.
    """

    def __init__(self):
        self.__graph = None
        self.__id = None

    @property
    def linked_graph(self):
        return self.__graph

    @property
    def linked_id(self):
        return self.__id

    @property
    def linked(self):
        return self.__graph is not None and self.__id is not None

    def link(self, graph, id):
        self.__graph = graph
        self.__id = id

    def unlink(self):
        self.__graph = None
        self.__id = None

    def __assert_linked(self):
        if not self.linked:
            raise NotLinkedError(self)

    def pull(self):
        self.__assert_linked()

    def push(self):
        self.__assert_linked()


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


class Node(Linkable, PropertyContainer, yaml.YAMLObject):
    """ A local representation of a Neo4j graph node that may be linked to a
    node in a remote graph database.
    """
    yaml_tag = '!Node'

    @classmethod
    def from_yaml(cls, loader, node):
        graph = loader.__graph__
        mapping = loader.construct_mapping(node, deep=True)
        labels = mapping.get("labels")
        properties = mapping.get("properties")
        inst = Node(*labels, **properties)
        id_ = mapping.get("id")
        if id_ is not None:
            inst.link(graph, id_)
        return inst

    def __init__(self, *labels, **properties):
        Linkable.__init__(self)
        PropertyContainer.__init__(self, **properties)
        self.__labels = set(labels or [])

    def __repr__(self):
        if self.linked:
            id_ = self.linked_id
        else:
            id_ = ""
        return "({0}{1} {2})".\
            format(id_, "".join(":" + label for label in self.__labels),
                   json.dumps(self.properties, separators=",:"))

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
        Linkable.pull(self)
        try:
            self.linked_graph.get_node(self.linked_id)
        except Error:  # TODO: NotExistsError
            return False
        else:
            return True

    def pull(self):
        Linkable.pull(self)
        remote = self.linked_graph.get_node(self.linked_id)
        self.__labels = set(remote.labels)
        self.properties.clear()
        self.properties.update(remote.properties)

    def push(self):
        Linkable.push(self)
        self.linked_graph.set_node(self.linked_id,
                                   list(self.__labels), self.properties)


class Relationship(Linkable, PropertyContainer, yaml.YAMLObject):
    yaml_tag = '!Rel'

    @classmethod
    def from_yaml(cls, loader, rel):
        graph = loader.__graph__
        mapping = loader.construct_mapping(rel, deep=True)
        start_node = mapping.get("start")
        type_ = mapping.get("type")
        end_node = mapping.get("end")
        properties = mapping.get("properties")
        inst = Relationship(*(start_node, type_, end_node), **properties)
        id_ = mapping.get("id")
        if id_ is not None:
            inst.link(graph, id_)
        return inst

    def __init__(self, *triple, **properties):
        Linkable.__init__(self)
        PropertyContainer.__init__(self, **properties)
        if len(triple) != 3:
            raise ValueError("Relationships constructors must specify a "
                             "start-type-end triple")
        self.__start_node = triple[0]
        self.__type = triple[1]
        self.__end_node = triple[2]
        if not isinstance(self.__start_node, Node):
            raise ValueError("Relationships must start with a Node object")
        if not isinstance(self.__end_node, Node):
            raise ValueError("Relationships must end with a Node object")

    def __repr__(self):
        if self.linked:
            id_ = self.linked_id
        else:
            id_ = ""
        return "-[{0}:{1} {2}]->".\
            format(id_, self.__type,
                   json.dumps(self.properties, separators=",:"))

    @property
    def start_node(self):
        return self.__start_node

    @property
    def end_node(self):
        return self.__end_node

    @property
    def type(self):
        return self.__type

    @property
    def exists(self):
        Linkable.pull(self)
        try:
            self.linked_graph.get_rel(self.linked_id)
        except Error:  # TODO: NotExistsError
            return False
        else:
            return True

    def pull(self):
        Linkable.pull(self)
        remote = self.linked_graph.get_rel(self.linked_id)
        self.properties.clear()
        self.properties.update(remote.properties)

    def push(self):
        Linkable.push(self)
        self.linked_graph.set_rel(self.linked_id, self.properties)


class Path(yaml.YAMLObject):
    yaml_tag = '!Path'
