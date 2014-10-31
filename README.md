[![Build Status](https://travis-ci.org/zerograph/zerograph.png)](https://travis-ci.org/zerograph/zerograph)

# Zerograph

## Quick Start

To run the server:

```bash
$ gradle run
```

To run the test shell:

```
$ bin/zerograph shell

Zerograph Shell v1β
(C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

Execute Cypher statements or meta-commands (prefixed with "!"). Multiple Cypher
statements can be separated by a semicolon and will be executed within the
same batched transaction. Type !HELP for help or type !EOF to exit the shell.

[Z] localhost:47470> !help
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

[Z] localhost:47470> create (a:Person {name:'Alice'}) return a
a
-----------------------------------
(1:Person {"name":"Alice"})
(1 row)


[Z] localhost:47470> ⌁
```
