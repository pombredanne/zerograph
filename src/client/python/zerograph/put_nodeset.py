#!/usr/bin/env python3

import json
import readline
import sys

import zmq


context = zmq.Context()

#  Socket to talk to server
socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:47474")


def send(verb, resource, *args):
    line = verb + "\t" + resource + "\t" + "\t".join(json.dumps(arg) for arg in args)
    print(">>> " + line)
    socket.send(line.encode("utf-8"))


def receive():
    message = socket.recv().decode("utf-8")
    print("<<< " + message)
    return message


def put_nodeset(label, key, value):
    send("PUT", "nodeset", "default", label, key, value)
    message = receive()
    while socket.getsockopt(zmq.RCVMORE):
        message = receive()

if __name__ == "__main__":
    label = sys.argv[1]
    key = sys.argv[2]
    value = sys.argv[3]
    put_nodeset(label, key, value)
