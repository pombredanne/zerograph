#!/usr/bin/env python

import json
import logging
import readline
import sys

import zmq

from .batch import Batch
from .types import hydrate


log = logging.getLogger(__name__)
log.addHandler(logging.NullHandler())


class Session(object):

    def __init__(self, address):
        self.__address = address
        self.__context = zmq.Context()
        self.__socket = self.__context.socket(zmq.REQ)
        self.__socket.connect(self.__address)

    def batch(self):
        return Batch(self.__socket)

    def __request(self, method, resource, *args):
        # send
        line = method + "\t" + resource + "\t" + "\t".join(json.dumps(arg) for arg in args)
        self.__socket.send(line.encode("utf-8"))
        # receive
        frames = [self.__socket.recv().decode("utf-8")]
        while self.__socket.getsockopt(zmq.RCVMORE):
            frames.append(self.__socket.recv().decode("utf-8"))
        response_lines = "\n".join(frames).splitlines(keepends=False)
        for line in response_lines:
            if line:
                parts = line.split("\t")
                yield (int(parts[0]), tuple(hydrate(part) for part in parts[1:]))

    def execute(self, query):
        for rs in self.__request("POST", "cypher", query):
            yield rs[1]

    def create_node(self, labels, properties):
        rs = list(self.__request("POST", "node", list(labels), dict(properties)))
        return rs[0][1][0]

    def delete_node(self, id):
        list(self.__request("DELETE", "node", id))
