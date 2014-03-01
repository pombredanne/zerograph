package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

public class NodeResource extends Resource {

    final public static String NAME = "node";

    public NodeResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public void get(Transaction transaction, Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        try {
            Node node = database().getNodeById(nodeID);
            sendOK(node);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, nodeID));
        }
    }

    /**
     * PUT node {nid} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public void put(Transaction transaction, Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        List labelNames = getArgument(request, 1, List.class);
        Map properties = getArgument(request, 2, Map.class);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = transaction.acquireWriteLock(node);
            Lock readLock = transaction.acquireReadLock(node);
            removeLabels(node);
            removeProperties(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, nodeID));
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
    public void patch(Transaction transaction, Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        List labelNames = getArgument(request, 1, List.class);
        Map properties = getArgument(request, 2, Map.class);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = transaction.acquireWriteLock(node);
            Lock readLock = transaction.acquireReadLock(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, nodeID));
        }
    }

    /**
     * POST node {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public void post(Transaction transaction, Request request) throws ClientError, ServerError {
        List labelNames = getArgument(request, 0, List.class);
        Map properties = getArgument(request, 1, Map.class);
        Node node = database().createNode();
        Lock writeLock = transaction.acquireWriteLock(node);
        Lock readLock = transaction.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        sendOK(node);
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public void delete(Transaction transaction, Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        Node node = database().getNodeById(nodeID);
        Lock writeLock = transaction.acquireWriteLock(node);
        node.delete();
        writeLock.release();
        sendOK();
    }

    public static void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            node.addLabel(DynamicLabel.label(labelName.toString()));
        }
    }

    public static void removeLabels(Node node) {
        for (Label label : node.getLabels()) {
            node.removeLabel(label);
        }
    }

    public static void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    public static void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
