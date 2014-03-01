#!/usr/bin/env python


from __future__ import unicode_literals

import json

import zmq

from .types import hydrate


class Batch(object):

    def __init__(self, socket):
        self.__socket = socket

    def do(self, method, resource, *args):
        line = method + "\t" + resource + "\t" + "\t".join(json.dumps(arg) for arg in args)
        self.__socket.send(line.encode("utf-8"), zmq.SNDMORE)

    def do_execute(self, query):
        self.do("POST", "cypher", query)

    def do_create_node(self, labels, properties):
        self.do("POST", "node", labels, properties)

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
