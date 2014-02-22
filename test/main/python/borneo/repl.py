#!/usr/bin/env python3

import json
import readline

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


def post_cypher(query):
    send("POST", "cypher", query)
    message = receive()
    while message.startswith("100"):
        message = receive()


if __name__ == "__main__":
    done = False
    while not done:
        line = input("\x1b[32;1mborneo>\x1b[0m ")
        if line.lower() == "quit":
            done = True
        else:
            post_cypher(line)
