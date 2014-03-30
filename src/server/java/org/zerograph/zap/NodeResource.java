package org.zerograph.zap;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

import java.util.List;
import java.util.Map;

public class NodeResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Node";

    public NodeResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET Node {"id": 1}
     *
     * Fetch a single node by ID.
     */
    @Override
    public PropertyContainer get(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        try {
            Node node = database.getNode(id);
            responder.sendBody(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

    /**
     * SET Node {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer set(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        List labelNames = request.getArgumentAsList("labels");
        Map properties = request.getArgumentAsMap("properties");
        try {
            Node node = database.putNode(id, labelNames, properties);
            responder.sendBody(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

    /**
     * PATCH node {node_id} {labels} {properties}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        List labelNames = request.getArgumentAsList("labels");
        Map properties = request.getArgumentAsMap("properties");
        try {
            Node node = database.patchNode(id, labelNames, properties);
            responder.sendBody(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

    /**
     * POST node {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public PropertyContainer create(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        List labelNames = request.getArgumentAsList("labels");
        Map properties = request.getArgumentAsMap("properties");
        Node node = database.createNode(labelNames, properties);
        responder.sendBody(node);
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        try {
            database.deleteNode(id);
            return null;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

}
