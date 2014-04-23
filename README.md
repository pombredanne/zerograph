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
opened on other ports as required and dropped when no longer needed. On
startup, all configured graph services will be started on their respective
ports.

One big benefit on the ZeroMQ infrastructure is a reduced impact to client
applications when server disruption occurs. When a client submits a request,
the server may or may not be available. If unavailable, the request will be
queued automatically until the server once again becomes available at which
point it will be processed as usual. No extra logic is required within the
client application to manage retries.


## Shell

To run the test shell (Python 3 preferred):

```
$ bin/zerograph shell

Zerøgraph Shell v0
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Multiple Cypher
statements can be separated by a semicolon and will be executed within the
same batched transaction. Type !HELP for help or type !EOF or press Ctrl+D to
exit the shell.

(Z) localhost:47470> !help
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

(Z) localhost:47470> create (a:Person {name:'Alice'}) return a
a
-----------------------------------
(123456:Person {"name": "Alice"})
(1 row)


(Z) localhost:47470> !eof
⌁
```
