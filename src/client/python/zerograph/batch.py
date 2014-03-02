#!/usr/bin/env python


from __future__ import unicode_literals

import zmq

from .types import hydrate, dehydrate, Pointer


class Batch(object):

    def __init__(self, socket):
        self.__socket = socket
        self.__count = 0

    def do(self, method, resource, *args):
        line = method + "\t" + resource + "\t" + "\t".join(map(dehydrate, args))
        self.__socket.send(line.encode("utf-8"), zmq.SNDMORE)
        pointer = Pointer(self.__count)
        self.__count += 1
        return pointer

    def do_execute(self, query):
        return self.do("POST", "cypher", query)

    def do_create_node(self, labels, properties):
        return self.do("POST", "node", labels, properties)

    def do_create_rel(self, start_node, end_node, type, properties):
        return self.do("POST", "rel", start_node, end_node, type, properties)

    def submit(self):
        self.__socket.send(b"")
        frames = [self.__socket.recv().decode("utf-8")]
        while self.__socket.getsockopt(zmq.RCVMORE):
            frames.append(self.__socket.recv().decode("utf-8"))
        response_lines = "\n".join(frames).splitlines(keepends=False)
        for line in response_lines:
            if line:
                parts = line.split("\t")
                status = int(parts[0])
                yield (status, tuple(hydrate(part) for part in parts[1:]))
