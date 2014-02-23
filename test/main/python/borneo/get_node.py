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


def get_node(node_id):
    send("GET", "node", "default", node_id)
    message = receive()
    while message.startswith("100"):
        message = receive()


if __name__ == "__main__":
    node_id = int(sys.argv[1])
    get_node(node_id)
