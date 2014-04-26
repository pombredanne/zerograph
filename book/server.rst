======
Server
======

The Zerograph server can operate a number of concurrent graph database services
across a range of ports but the default service listens on port 47470. This
service must be maintained to keep any others alive although others may be
spawned and dropped as required. On server startup, all pre-existing services
will be restarted on their respective ports.

To run the server::

    $ gradle run

If running as root, the databases created will be stored in ``/var/zerograph``
by default. For other users, the ``$HOME/.zerograph`` directory will be used.
These defaults can be overridden using the ``ZEROGRAPH_HOME`` environment
variable.

A big benefit of the ZeroMQ infrastructure is a reduced impact to client
applications when server disruption occurs. When a client submits a request,
the server may or may not be available. If unavailable, the request will be
queued automatically until the server once again becomes available at which
point it will be processed as usual. No extra logic is required within the
client application to manage retries.

The Zerograph Application/Presentation Protocol (ZAPP) is used for all
communication between the client and the server. Details of this protocol can
be seen in the `protocol specification document`_.


Resources
=========

The server exposes a number of resources, each of which provides a specific
set of functionality.

Cypher
------
The *Cypher* resource provides an endpoint against which Cypher queries can be
executed.

.. function:: EXECUTE Cypher {"query": …, "params": …}

   Execute a Cypher query with a set of named parameters.

Graph
-----
The *Graph* resource represents a Neo4j graph database exposed as a Zerograph
service.

.. function:: GET Graph {"host": …, "port": …}

   Fetch a representation of the specified graph database service, if such a
   service exists.

.. function:: PATCH Graph {"host": …, "port": …}

   Fetch a representation of the specified graph database service, creating a
   new graph if none exists.

.. function:: DELETE Graph {"host": …, "port": …}

   Drop the graph database instance bound to the port specified.

Node
----
The *Node* resource represents an individual Neo4j graph database node
identified by internal node ID.

.. function:: GET Node {"id": …}

   Fetch the node identified by id.

.. function:: SET Node {"id": …, "labels": …, "properties": …}

   Replace the labels and properties on the node identified by id and return
   the updated node.

.. function:: PATCH Node {"id": …, "labels": …, "properties": …}

   Supplement the labels and properties on the node identified by id and return
   the updated node.

.. function:: CREATE Node {"labels": …, "properties": …}

   Create and return a new node with the labels and properties specified.

.. function:: DELETE Node {"id": …}

   Delete the node identified by id.

NodeSet
-------
The *NodeSet* resource represents a group of nodes that share a common label
and (optionally) property.

.. function:: GET NodeSet {"label": …, "key": …, "value": …}

   Fetch all nodes with the specified label, property key and property value.
   If key is null or missing, only the label will be used for matching.

.. function:: PATCH NodeSet {"label": …, "key": …, "value": …}

   Create a node with the specified label and property if none exists. Return
   all nodes with these criteria.

.. function:: DELETE NodeSet {"label": …, "key": …, "value": …}

   Delete all nodes with the specified label, property key and property value.
   If key is null or missing, all nodes with that label will be deleted.

Path
----
...

Rel
---
The *Rel* resource represents an individual Neo4j graph database relationship
identified by internal relationship ID.

.. function:: GET Rel {"id": …}

   Fetch the path segment containing the relationship identified by `id`.

.. function:: SET Rel {"id": …, "properties": …}

   Replace the properties on the relationship identified by `id` and return the
   path segment containing the updated relationship.

.. function:: PATCH Rel {"id": …, "properties": …}

   Supplement the properties on the relationship identified by `id` and return
   the path segment containing the updated relationship.

.. function:: CREATE Rel {"start": …, "end": …, "type": …, "properties": …}

   Create a new relationship with the type and properties specified and return
   the surrounding path segment.

.. function:: DELETE Rel {"id": …}

   Delete the relationship identified by `id`.

RelSet
------
The *RelSet* resource represents a group of relationships that share common end
points and/or type.

.. function:: GET RelSet {"start": …, "end": …, "type": …}

   Fetch all path segments that contain relationships with the specified end
   points and/or type. All criteria can be null or missing but at least one end
   point must be provided.

.. function:: PATCH RelSet {"start": …, "end": …, "type": …}

   Ensure at least one relationship exists with the specified end points and
   type and return all matching path segments. All criteria must be provided.

.. function:: DELETE RelSet {"start": …, "end": …, "type": …}

   Delete all path segments that contain relationships with the specified end
   points and/or type. All criteria can be null or missing but at least one end
   point must be provided.

Zerograph
---------
The *Zerograph* resource represents the entire Zerograph server application and
can be used to retrieve details about the services available and system
variables.

.. function:: GET Zerograph {}

   Fetch details of the Zerograph server application.


.. _protocol specification document: zapp.html
