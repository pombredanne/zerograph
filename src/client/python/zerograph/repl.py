#!/usr/bin/env python3
# -*- encoding: utf-8 -*-

import json
import readline

import zmq


context = zmq.Context()

#  Socket to talk to server
socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:47474")


def send(verb, resource, data, flags):
    line = verb + "\t" + resource + "\t" + "\t".join(json.dumps(datum) for datum in data)
    print(">>> " + line)
    socket.send(line.encode("utf-8"), flags)


def receive():
    message = socket.recv().decode("utf-8")
    print("<<< " + message)
    return message


if __name__ == "__main__":
    try:
        count = 0
        done = False
        while not done:
            line = input("\x1b[32;1mzerograph>\x1b[0m ")
            if line.lower() == "quit":
                done = True
            else:
                if line.endswith("&"):
                    line = line.rstrip("&").rstrip()
                    flags = zmq.SNDMORE
                else:
                    flags = 0
                    count = 0
                send("POST", "cypher", [line], flags)
                count += 1
                if flags == 0:
                    for i in range(count):
                        message = receive()
                        while socket.getsockopt(zmq.RCVMORE):
                            message = receive()
    except EOFError:
        print("⌁")