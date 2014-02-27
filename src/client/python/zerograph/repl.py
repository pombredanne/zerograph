#!/usr/bin/env python3
# -*- encoding: utf-8 -*-

import json
import readline

import zmq


context = zmq.Context()

#  Socket to talk to server
socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:47474")


def send(verb, resource, data, commit):
    line = verb + "\t" + resource + "\t" + "\t".join(json.dumps(datum) for datum in data)
    print(">>> " + line)
    if commit:
        flags = 0
    else:
        flags = zmq.SNDMORE
    socket.send(line.encode("utf-8"), flags)


def receive():
    message = socket.recv().decode("utf-8")
    print("<<< " + message)
    return message


def post_cypher(query, commit):
    send("POST", "cypher", [query], commit)
    message = receive()
    while message.startswith("100"):
        message = receive()


if __name__ == "__main__":
    try:
        done = False
        while not done:
            line = input("\x1b[32;1mzerograph>\x1b[0m ")
            if line.lower() == "quit":
                done = True
            else:
                if line.endswith("&"):
                    line = line.rstrip("&").rstrip()
                    commit = False
                else:
                    commit = True
                post_cypher(line, commit)
    except EOFError:
        print("⌁")