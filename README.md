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
These defaults can be overridden using the ``ZEROGRAPH_HOME`` environment
variable.

### Service

The default service (graph zero) listens on port 47470. Graph services may be
created on other ports as required and dropped if necessary.


## Shell

To run the test shell (Python 3 preferred):

```
$ bin/zerograph-shell

Zerøgraph Shell v0
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Multiple Cypher
statements can be separated by a semicolon and will be executed within the
same batched transaction. Type !HELP for help or type !EOF or press Ctrl+D to
exit the shell.

(Z) localhost:47470> !help
!HELP           display this help
!EOF            exit the shell

!OPEN <port>    open graph on port <port>, creating it if it doesn't exist
!CLOSE <port>   close graph on port <port> and delete the files from disk

!GET <resource> [<json_object_data>]
!SET <resource> [<json_object_data>]
!PATCH <resource> [<json_object_data>]
!CREATE <resource> [<json_object_data>]
!DELETE <resource> [<json_object_data>]
!EXECUTE <resource> [<json_object_data>]

(Z) localhost:47470> create (a:Person {name:'Alice'}) return a
a
-----------------------------------
(123456:Person {"name": "Alice"})
(1 row)


(Z) localhost:47470> !eof
⌁
```
