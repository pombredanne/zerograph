#!/usr/bin/env python3


import sys

from zerograph.client import Client


if __name__ == "__main__":
    client = Client("tcp://localhost:47474")
    for line in client.send("GET", "nodeset", "default", *sys.argv[1:]):
        print(line)
