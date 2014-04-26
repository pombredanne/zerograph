=============
Python Client
=============

.. py:currentmodule:: zerograph


Connecting to a Graph
=====================

To connect to a graph, use the :func:`Graph.open` method. If no arguments are
supplied to this method, defaults of ``'localhost'`` and ``47470`` are assumed.

::

    from zerograph import Graph
    graph = Graph.open()


.. autoclass:: Graph
   :members:


Nodes, Rels & Paths
===================

Graph database entities are primarily represented using the :class:`Node`,
:class:`Rel` and :class:`Path` classes.

.. autoclass:: Node
   :members:

.. autoclass:: Rel
   :members:

.. autoclass:: Rev
   :members:

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
