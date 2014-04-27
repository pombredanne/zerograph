=============
Python Client
=============

.. py:currentmodule:: zerograph


Connecting to a Graph
=====================

To connect to a graph, use the :func:`Graph.open` method. If no arguments are
supplied to this method, defaults of ``'localhost'`` and ``47470`` are assumed.

::

    >>> from zerograph import *
    >>> graph = Graph.open()
    >>> alice, bob = graph.create(Node(name="Alice"), Node(name="Bob"))
    >>> alice
    (118643 {"name":"Alice"})
    >>> bob
    (118644 {"name":"Bob"})
    >>> alice_bob, = graph.create(Relationship(alice, "KNOWS", bob))
    >>> alice_bob
    (118643 {"name":"Alice"})-[161512:KNOWS]->(118644 {"name":"Bob"})

.. autoclass:: Graph
   :members: open, drop, host, port, zerograph, order, size, clear, execute,
             node, relationship, pull, push, create, delete


Nodes, Rels, Paths & more
=========================

Graph database entities are primarily represented using the :class:`Node`,
:class:`Rel` and :class:`Path` classes.

.. autoclass:: Node
   :members: labels, replace, exists, pull, push, to_cypher, to_geoff,
   :inherited-members: properties, bound, graph, _id
   :no-members: to_yaml

.. autoclass:: Rel(type, **properties)
   :members: type
   :inherited-members: replace
   :no-members: to_yaml

.. autoclass:: Rev(type, **properties)
   :members:
   :no-members: to_yaml

.. autoclass:: Path
   :members:

.. autoclass:: Relationship
   :members:


Batches
=======

.. autoclass:: Batch
   :members:

.. autoclass:: Pointer
   :members:

.. autoclass:: BatchPull
   :members:

.. autoclass:: BatchPush
   :members:
