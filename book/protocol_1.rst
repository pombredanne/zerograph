==========================
Protocol Specification V1β
==========================

1. Introduction
===============

The Zerograph Application/Presentation Protocol (ZAPP) provides an OSI level
6/7 text-based request-response protocol that is used to facilitate
communications between client applications that require a graph database and
server applications that provide a Neo4j data store.

The reference implementation for ZAPP (the Zerograph server bundle) transmits
all messages over ZeroMQ_. This means of transmission is independent to the
protocol however and any request-response mechanism that supports textual
messaging should be able to support ZAPP.


2. Requirements
===============

2.1. Encoding
-------------
All requests and responses MUST only use characters from the basic ASCII set,
i.e. 0x00 to 0x7F. Extended characters can be represented with the appropriate
JSON or YAML encoding: generally the "\uXXXX" sequence.

2.2. End of Line Sequences
--------------------------
Lines may be separated by any common end-of-line sequence as defined below::

    eol := <CR> | <LF> | <CR><LF>


.. _ZeroMQ: http://zeromq.org/
