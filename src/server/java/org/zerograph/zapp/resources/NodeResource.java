package org.zerograph.zapp.resources;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.ServerError;

import java.util.ArrayList;
import java.util.HashMap;
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
     * GET Node {"id": ?}
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
     * SET Node {"id": ?, "labels": ?, "properties": ?}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer set(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        List labelNames = request.getArgumentAsList("labels");
        Map<String, Object> properties = request.getArgumentAsMap("properties");
        try {
            Node node = database.putNode(id, labelNames, properties);
            responder.sendBody(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

    /**
     * PATCH Node {"id": ?, "labels": ?}
     * PATCH Node {"id": ?, "properties": ?}
     * PATCH Node {"id": ?, "labels": ?, "properties": ?}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        List labelNames = request.getArgumentAsList("labels", new ArrayList());
        Map<String, Object> properties = request.getArgumentAsMap("properties", new HashMap<String, Object>());
        try {
            Node node = database.patchNode(id, labelNames, properties);
            responder.sendBody(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError("Node " + id + " not found");
        }
    }

    /**
     * CREATE Node {"labels": ?, "properties": ?}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public PropertyContainer create(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        List labelNames = request.getArgumentAsList("labels");
        Map<String, Object> properties = request.getArgumentAsMap("properties");
        Node node = database.createNode(labelNames, properties);
        responder.sendBody(node);
        return node;
    }

    /**
     * DELETE node {"id": ?}
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
