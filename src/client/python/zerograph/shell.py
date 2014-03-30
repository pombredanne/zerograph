#!/usr/bin/env python3
# -*- encoding: utf-8 -*-


from __future__ import print_function, unicode_literals

import codecs
import locale
import readline
import sys

from .zerograph import *


WELCOME = """\

\x1b[37;1mZerø\x1b[32;1mgraph\x1b[0m Shell v0
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Multiple Cypher
statements can be separated by a semicolon and will be executed within the
same batched transaction. Type !HELP for help or type !EOF or press Ctrl+D to
exit the shell.

"""

HELP = """\
!HELP           display this help
!EOF            exit the shell

!OPEN <port>    open graph on port <port>, creating it if it doesn't exist
!CLOSE <port>   close graph on port <port>
!DROP <port>    close graph on port <port> and delete it

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

    @property
    def graph(self):
        return self.__graph

    @property
    def prompt(self):
        return "\x1b[32;1mzg:\x1b[34;1m{0}:{1}>\x1b[0m ".format(self.graph.host, self.graph.port)

    def print_error(self, message):
        print("\x1b[33m{0}\x1b[0m".format(message))

    def meta(self, line):
        command, args = line[1:].partition(" ")[0::2]
        command = command.upper()
        if command == "EOF":
            raise EOFError()
        elif command == "HELP":
            self.help()
        elif command == "OPEN":
            port = int(args.partition(" ")[0])
            self.__graph = self.__graph.open_graph(port)
        elif command == "CLOSE":
            port = int(args.partition(" ")[0])
            self.__graph.close_graph(port)
        elif command == "DROP":
            port = int(args.partition(" ")[0])
            self.__graph.close_graph(port, delete=True)
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

    def repl(self):
        while True:
            try:
                line = get_input(self.prompt)
                if line.startswith("!"):
                    self.meta(line)
                else:
                    batch = self.graph.create_batch()
                    for query in line.split(";"):
                        batch.execute(query.strip())
                    try:
                        results = batch.submit()
                        #rs = self.graph.execute(line)
                    except ErrorResponse as err:
                        self.print_error(err.args[0])
                    else:
                        for result in results:
                            print(result.to_table())
            except EOFError:
                print("⌁")
                break
            #except Exception as err:
            #    self.print_error(err)
            print()


if __name__ == "__main__":
    shell = Shell(Graph.zero("localhost"))
    shell.welcome()
    shell.repl()
