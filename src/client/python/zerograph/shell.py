#!/usr/bin/env python3
# -*- encoding: utf-8 -*-


from __future__ import print_function, unicode_literals

import codecs
import locale
import readline
import sys

from . import *


HOST = "localhost"

WELCOME = """\

Zerograph Shell v1β
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Multiple Cypher
statements can be separated by a semicolon and will be executed within the
same batched transaction. Type !HELP for help or type !EOF to exit the shell.

"""

HELP = """\
!HELP           display this help
!EOF            exit the shell

!OPEN <port>    open the graph on port <port>, creating it if it doesn't exist
!DROP <port>    drop the graph on port <port> and delete the files from disk

!GET <resource> [<json_object_data>]
!SET <resource> [<json_object_data>]
!PATCH <resource> [<json_object_data>]
!CREATE <resource> [<json_object_data>]
!DELETE <resource> [<json_object_data>]
!EXECUTE <resource> [<json_object_data>]
"""

if sys.version_info >= (3,):

    def get_input(prompt):
        return input(prompt)

else:

    _stdin = sys.stdin
    preferred_encoding = locale.getpreferredencoding()
    sys.stdin = codecs.getreader(preferred_encoding)(sys.stdin)
    sys.stdout = codecs.getwriter(preferred_encoding)(sys.stdout)
    sys.stderr = codecs.getwriter(preferred_encoding)(sys.stderr)

    def get_input(prompt):
        sys.stdin = _stdin
        return raw_input(prompt).decode(sys.stdin.encoding)


class Shell(object):

    def __init__(self, graph):
        self.__graph = graph
        self.__queries = {}

    @property
    def graph(self):
        return self.__graph

    @property
    def prompt(self):
        return "[Z] {0}:{1}> ".format(self.graph.host, self.graph.port)

    def print_error(self, message):
        print(message)

    def meta(self, line):
        command, args = line[1:].partition(" ")[0::2]
        command = command.upper()
        if command == "EOF":
            raise EOFError()
        elif command == "HELP":
            self.help()
        elif command == "OPEN":
            port = int(args.partition(" ")[0])
            self.__graph = Graph.open(HOST, port)
        elif command == "DROP":
            port = int(args.partition(" ")[0])
            if port == 47470:
                self.print_error("Cannot drop zerograph on port 47470")
            else:
                Graph.open(HOST, port).drop()
                print("Dropped graph on port {0}".format(port))
        elif command == "RECORD":
            name, query = args.partition(" ")[0::2]
            self.__queries[name] = query
            print("Query {0} recorded".format(repr(name)))
        elif command == "PLAY":
            name, param_sets = args.partition(" ")[0::2]
            param_sets = json.loads(param_sets)
            try:
                query = self.__queries[name]
            except KeyError:
                self.print_error("No query named {0} recorded".format(repr(name)))
            else:
                print("Playing query {0}...\n".format(repr(name)))
                self.execute_query(query, param_sets)
        elif command in ("GET", "SET", "PATCH", "CREATE", "DELETE", "EXECUTE"):
            resource, arguments = args.partition(" ")[0::2]
            try:
                arguments = json.loads(arguments)
            except ValueError:
                self.print_error("Bad JSON: " + arguments)
            else:
                if isinstance(arguments, dict):
                    result = Batch.single(self.graph, Batch.append, command, resource, **arguments)
                    print(result)
                else:
                    self.print_error("Not a JSON object: {0}".format(arguments))
        else:
            self.print_error("Unknown meta-command: !" + command)

    def welcome(self):
        sys.stdout.write(WELCOME)

    def help(self):
        sys.stdout.write(HELP)

    def _submit(self, batch):
        try:
            results = batch.submit()
        except Error as err:
            self.print_error(err.args[0])
        else:
            for result in results:
                print(result)

    def execute_query(self, query, param_sets):
        batch = Batch(self.graph)
        for params in param_sets:
            batch.execute_cypher(query, params)
        self._submit(batch)

    def execute_queries(self, line):
        batch = Batch(self.graph)
        for query in line.split(";"):
            query = query.strip()
            if query:
                batch.execute_cypher(query.strip())
        self._submit(batch)

    def repl(self):
        while True:
            try:
                line = get_input(self.prompt)
                if line.startswith("!"):
                    self.meta(line)
                elif all(map(lambda ch: ch.isdigit(), line.strip())):
                    print(self.graph.node(int(line.strip())))
                else:
                    self.execute_queries(line)
            except EOFError:
                print("⌁")
                break
            except Exception as err:
                self.print_error(err)
            print()


def main():
    shell = Shell(Graph.open(HOST))
    shell.welcome()
    shell.repl()
