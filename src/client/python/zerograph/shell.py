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

Execute Cypher statements or meta-commands (prefixed with "!"). Type !HELP for
help or type !EOF or press Ctrl+D to exit the shell.

"""

HELP = """\
!HELP           display this help
!EOF            exit the shell

!OPEN <port>    open graph on port <port>
!OPEN++ <port>  open graph on port <port>, creating it if it doesn't exist
!CLOSE          close current graph
!CLOSE--        close current graph and delete it

!GET <resource> <arg_list>
!PUT <resource> <arg_list>
!PATCH <resource> <arg_list>
!POST <resource> <arg_list>
!DELETE <resource> <arg_list>
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

    def __init__(self, zerograph):
        self.__zerograph = zerograph
        self.__graph = None

    @property
    def zerograph(self):
        return self.__zerograph

    @property
    def graph(self):
        return self.__graph

    @property
    def prompt(self):
        if self.__graph:
            return "\x1b[32;1mzg:\x1b[34;1m{0}:{1}>\x1b[0m ".format(self.graph.host, self.graph.port)
        else:
            return "\x1b[32;1mzg:>\x1b[0m "

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
            self.__graph = self.__zerograph.open_graph(port)
        elif command == "OPEN++":
            port = int(args.partition(" ")[0])
            self.__graph = self.__zerograph.open_graph(port, create=True)
        elif command == "CLOSE":
            if self.graph:
                self.__zerograph.close_graph(self.__graph.port)
                self.__graph = None
            else:
                self.print_error("Not attached to a graph - use !OPEN <port> to connect.")
        elif command == "CLOSE--":
            if self.graph:
                self.__zerograph.close_graph(self.__graph.port, delete=True)
                self.__graph = None
            else:
                self.print_error("Not attached to a graph - use !OPEN <port> to connect.")
        elif command in ("GET", "SET", "PATCH", "CREATE", "DELETE", "EXECUTE"):
            resource, arg_list = args.partition(" ")[0::2]
            try:
                arg_list = json.loads(arg_list)
            except ValueError:
                self.print_error("Bad JSON: " + arg_list)
            else:
                if not isinstance(arg_list, list):
                    arg_list = [arg_list]
                if self.graph:
                    result = GraphBatch.single(self.graph.socket, GraphBatch.prepare, Response.atom, command ,resource, *arg_list)
                else:
                    result = ZerographBatch.single(self.zerograph.socket, ZerographBatch.prepare, Response.atom, command ,resource, *arg_list)
                print(result)
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
                elif not self.graph:
                    self.print_error("Not attached to a graph - use !OPEN <port> to connect.")
                else:
                    try:
                        rs = self.graph.execute(line)
                    except ClientError as err:
                        self.print_error(err.args[0])
                    else:
                        print(rs)
            except EOFError:
                print("⌁")
                break
            except Exception as err:
                self.print_error(err)
            print()


if __name__ == "__main__":
    shell = Shell(Zerograph())
    shell.welcome()
    shell.repl()
