#!/usr/bin/env python

import json
import logging
import readline
import sys

import zmq

from zerograph.types import hydrate


log = logging.getLogger(__name__)
log.addHandler(logging.NullHandler())


class Session(object):

    def __init__(self, address):
        self.__address = address
        self.__context = zmq.Context()
        self.__socket = self.__context.socket(zmq.REQ)
        self.__socket.connect(self.__address)

    def __receive_line(self):
        line = self.__socket.recv().decode("utf-8")
        log.info("<<< " + line)
        parts = line.split("\t")
        return int(parts[0]), tuple(hydrate(part) for part in parts[1:])

    def __send(self, method, resource, *args):
        line = method + "\t" + resource + "\t" + "\t".join(json.dumps(arg) for arg in args)
        log.info(">>> " + line)
        self.__socket.send(line.encode("utf-8"))
        yield self.__receive_line()
        while self.__socket.getsockopt(zmq.RCVMORE):
            yield self.__receive_line()

    def execute(self, database, query):
        for rs in self.__send("POST", "cypher", database, query):
            yield rs[1]

    def create_node(self, database, labels, properties):
        rs = list(self.__send("POST", "node", database, list(labels), dict(properties)))
        return rs[0][1][0]

    def delete_node(self, database, id):
        list(self.__send("DELETE", "node", database, id))
