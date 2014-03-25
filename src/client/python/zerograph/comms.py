
from __future__ import unicode_literals

import json

import yaml
import zmq


class Request(object):

    METHODS = ("GET", "SET", "PATCH", "CREATE", "DELETE", "EXECUTE")

    def __init__(self, method, resource, **data):
        self.__method = method.upper()
        if self.__method not in self.METHODS:
            raise ValueError("Unsupported method: " + self.__method)
        self.__resource = resource
        self.__data = data
        self.__json_data = json.dumps(data, separators=",:", ensure_ascii=True)

    def __repr__(self):
        return "<Request method={} resource={} " \
               "data={}>".format(repr(self.__method), repr(self.__resource),
                                 repr(self.__data))

    def __str__(self):
        return " ".join((self.__method, self.__resource, self.__json_data))

    @property
    def method(self):
        return self.__method

    @property
    def resource(self):
        return self.__resource

    @property
    def data(self):
        return self.__data

    def send(self, socket, more=False):
        socket.send(str(self).encode("utf-8"), zmq.SNDMORE if more else 0)
