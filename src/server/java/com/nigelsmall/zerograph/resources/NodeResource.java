package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

import static com.nigelsmall.zerograph.util.Helpers.*;

public class NodeResource extends Resource {

    final public static String NAME = "node";

    public NodeResource(GraphDatabaseService database, Transaction transaction, ZMQ.Socket socket) {
        super(database, transaction, socket);
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public void get(Request request) throws ClientError, ServerError {
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
    public void put(Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        List labelNames = getArgument(request, 1, List.class);
        Map properties = getArgument(request, 2, Map.class);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = acquireWriteLock(node);
            Lock readLock = acquireReadLock(node);
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
    public void patch(Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        List labelNames = getArgument(request, 1, List.class);
        Map properties = getArgument(request, 2, Map.class);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = acquireWriteLock(node);
            Lock readLock = acquireReadLock(node);
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
    public void post(Request request) throws ClientError, ServerError {
        List labelNames = getArgument(request, 0, List.class);
        Map properties = getArgument(request, 1, Map.class);
        Node node = database().createNode();
        Lock writeLock = acquireWriteLock(node);
        Lock readLock = acquireReadLock(node);
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
    public void delete(Request request) throws ClientError, ServerError {
        long nodeID = getArgument(request, 0, Integer.class);
        Node node = database().getNodeById(nodeID);
        Lock writeLock = acquireWriteLock(node);
        node.delete();
        writeLock.release();
        sendOK();
    }

}
