=========
Zerograph
=========

Zerograph is an alternative server container for the leading graph database
Neo4j_. It can support one or more databases and uses ZeroMQ_ for fast and
reliable communication.

The software is packaged as a bundle containing both the server and the client.
Initially, a Python client is provided although support for other languages
will follow.


Quick Start
===========

Download & Run
--------------
::

    $ git clone git@github.com:zerograph/zerograph.git
    $ cd zerograph
    $ gradle run

Execute a Cypher Query
----------------------
::

    $ bin/zerograph-shell
    Zerøgraph Shell v0
    (C) Copyright 2014, Nigel Small <nigel@nigelsmall.com>

    (Z) localhost:47470> create (a:Person {name:'Alice'}) return a
    a
    -----------------------------------
    (_0:Person {name:"Alice"})
    (1 row)


    (Z) localhost:47470> !eof
    ⌁


In Depth
========

.. toctree::
   :maxdepth: 2

   server
   python_client
   protocol_1


.. _Neo4j: http://www.neo4j.org/
.. _ZeroMQ: http://zeromq.org/
