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


class ErrorResponse(Exception):
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
        for document in yaml.load_all(full, Loader=GraphLoader):
            yield Response(document)

    def __init__(self, document):
        self.__head = document.get("head")
        self.__body = document.get("body")
        self.__foot = document.get("foot")
        if "error" in document:
            raise ErrorResponse(document["error"])

    def __repr__(self):
        s = ["Response"]
        if self.__head:
            s.append("head={0}".format(self.__head))
        if self.__body:
            s.append("body={0}".format(self.__body))
        if self.__foot:
            s.append("foot={0}".format(self.__foot))
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
            yield result
        self.__count = 0

    def get_graph(self, host, port):
        return self.append(GET, "Graph", host=host, port=int(port))

    def open_graph(self, host, port):
        return self.append(SET, "Graph", host=host, port=int(port))

    def close_graph(self, host, port):
        return self.append(DELETE, "Graph", host=host, port=int(port))

    def execute(self, query, params=None):
        return self.append(EXECUTE, "Cypher", query=query, params=dict(params or {}))

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

    def create_rel(self, start_node, end_node, type, properties=None):
        return self.append(CREATE, "Rel", start=start_node, end=end_node, type=type, properties=dict(properties or {}))

    def delete_rel(self, rel_id):
        return self.append(DELETE, "Rel", id=int(rel_id))

    def match_node_set(self, label, key, value):
        return self.append(GET, "NodeSet", label=label, key=key, value=value)

    def merge_node_set(self, label, key, value):
        return self.append(SET, "NodeSet", label=label, key=key, value=value)

    def purge_node_set(self, label, key, value):
        return self.append(DELETE, "NodeSet", label=label, key=key, value=value)


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

    __zero_instances = {}

    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        host = mapping.get("host")
        port = mapping.get("port")
        inst = cls(host=host, port=port)
        return inst

    @classmethod
    def zero(cls, host):
        try:
            return cls.__zero_instances[host]
        except KeyError:
            inst = cls(host=host, port=47470)
            cls.__zero_instances[host] = inst
            return inst

    @classmethod
    def open(cls, host, port):
        zero = cls.zero(host)
        return Batch.single(zero, Batch.open_graph, zero.host, port).body

    @classmethod
    def close(cls, host, port):
        zero = cls.zero(host)
        return Batch.single(zero, Batch.close_graph, zero.host, port).body

    def __init__(self, host, port):
        self.__host = host
        self.__port = port
        self.__address = "tcp://{0}:{1}".format(self.__host, self.__port)
        self.__context = zmq.Context()
        self.__socket = self.__context.socket(zmq.REQ)
        self.__socket.setsockopt(zmq.RCVTIMEO, 30000)  # TODO: configurable timeout
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
    def socket(self):
        return self.__socket

    def create_batch(self):
        return Batch(self)

    def execute(self, query):
        return Batch.single(self, Batch.execute, query).to_table()

    def get_node(self, node_id):
        return Batch.single(self, Batch.get_node, node_id).body

    def set_node(self, node_id, labels, properties):
        return Batch.single(self, Batch.set_node, node_id, labels, properties).body

    def patch_node(self, node_id, labels, properties):
        return Batch.single(self, Batch.patch_node, node_id, labels, properties).body

    def create_node(self, labels=None, properties=None):
        return Batch.single(self, Batch.create_node, labels, properties).body

    def delete_node(self, node_id):
        return Batch.single(self, Batch.delete_node, node_id).body

    def get_rel(self, rel_id):
        return Batch.single(self, Batch.get_rel, rel_id).body

    def set_rel(self, rel_id, properties):
        return Batch.single(self, Batch.set_rel, rel_id, properties).body

    def patch_rel(self, rel_id, properties):
        return Batch.single(self, Batch.patch_rel, rel_id, properties).body

    def create_rel(self, start_node, end_node, type, properties=None):
        return Batch.single(self, Batch.create_rel, start_node, end_node, type, properties).body

    def delete_rel(self, rel_id):
        return Batch.single(self, Batch.delete_rel, rel_id).body

    def match_node_set(self, label, key, value):
        return Batch.single(self, Batch.match_node_set, label, key, value).generator

    def merge_node_set(self, label, key, value):
        return Batch.single(self, Batch.merge_node_set, label, key, value).generator

    def purge_node_set(self, label, key, value):
        return Batch.single(self, Batch.purge_node_set, label, key, value).generator


class Node(yaml.YAMLObject):
    yaml_tag = '!Node'

    @classmethod
    def from_yaml(cls, loader, node):
        mapping = loader.construct_mapping(node, deep=True)
        labels = mapping.get("labels")
        properties = mapping.get("properties")
        inst = Node(labels, properties)
        id_ = mapping.get("id")
        if id_ is not None:
            inst.link(loader.__graph__, id_)
        return inst

    def __init__(self, labels=None, properties=None):
        self.__labels = set(labels or [])
        self.__properties = dict(properties or {})
        self.__graph = None
        self.__id = None

    def __repr__(self):
        if self.linked:
            return "<Node labels={0} properties={1} graph={2} id={3}>".format(self.__labels, self.__properties, self.__graph, self.__id)
        else:
            return "<Node labels={0} properties={1}>".format(self.__labels, self.__properties)

    def __str__(self):
        if self.linked:
            return "({0}{1} {2})".format(self.__id,
                                         "".join(":" + label for label in self.__labels),
                                         json.dumps(self.__properties)
            )

    @property
    def labels(self):
        return self.__labels

    @property
    def properties(self):
        return self.__properties

    @property
    def linked(self):
        return self.__graph is not None and self.__id is not None

    def link(self, graph, id_):
        self.__graph = graph
        self.__id = id_

    def unlink(self):
        self.__graph = None
        self.__id = None

    def __assert_linked(self):
        if not self.linked:
            raise NotLinkedError(self)

    def pull(self):
        self.__assert_linked()
        n = self.__graph.get_node(self.__id)
        self.__labels = set(n.labels)
        self.__properties = dict(n.properties)

    def push(self):
        self.__assert_linked()
        self.__graph.set_node(self.__id, list(self.__labels), self.__properties)


class Relationship(yaml.YAMLObject):
    yaml_tag = '!Rel'


class Path(yaml.YAMLObject):
    yaml_tag = '!Path'
