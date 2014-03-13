[![Build Status](https://travis-ci.org/zerograph/zerograph.png)](https://travis-ci.org/zerograph/zerograph)

# Zerograph

## Server

To run the server:

```bash
$ gradle run
```

### Storage

If running as root, the databases will be stored in ``/var/zerograph`` by
default. For other users, the ``$HOME/.zerograph`` directory will be used.
These defaults can be overridden using the ``ZG_STORAGE_PATH`` environment
variable.

### Service

The central Zerograph service instance listens on port 47470. Databases can be
started on any other ports as required.


## Shell

To run the test shell (Python 3 preferred):

```
$ bin/zerograph-shell

Zerøgraph Shell v0
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Type !HELP for
help or type !EOF or press Ctrl+D to exit the shell.

zg:> !help
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

zg:> !open++ 47471

zg:localhost:47471> !get node 0
Node 0 not found

zg:localhost:47471> create (a:Person {name:'Alice'}) return a
 a
------------------------------------------
 <Node id=0 labels=set(['Person']) properties={'name': 'Alice'}>

zg:localhost:47471> !get node 0
<Node id=0 labels=set(['Person']) properties={'name': 'Alice'}>

zg:localhost:47471> !eof
⌁
```
