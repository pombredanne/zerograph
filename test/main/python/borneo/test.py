#!/usr/bin/env python

from __future__ import print_function

import json
import zmq

context = zmq.Context()

#  Socket to talk to server
socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:47474")


def send(verb, resource, *args):
    line = verb + "\t" + resource + "\t" + "\t".join(json.dumps(arg) for arg in args)
    print(">>> " + line)
    socket.send(line.encode("utf-8"))

def recv():
    message = socket.recv().decode("utf-8")
    print("<<< " + message)
    return message

query = """\
MERGE (a:Person {name:'Alice'})
MERGE (b:Person {name:'Bob'})
CREATE UNIQUE (a)-[:KNOWS]->(b)
RETURN a, b
"""

send("POST", "cypher", query)

#  Get the reply.
message = recv()
while message.startswith("100"):
    message = recv()
