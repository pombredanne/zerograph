package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

public class NodeResource extends PropertyContainerResource {

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
    public PropertyContainer get(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            sendOK(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, nodeID));
        }
    }

    /**
     * PUT node {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            removeLabels(node);
            removeProperties(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
            return node;
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
    public PropertyContainer patch(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
            return node;
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
    public PropertyContainer post(Transaction tx, Request request) throws ClientError, ServerError {
        List labelNames = request.getListData(0);
        Map properties = request.getMapData(1);
        Node node = database().createNode();
        Lock writeLock = tx.acquireWriteLock(node);
        Lock readLock = tx.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        sendOK(node);
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        Node node = database().getNodeById(nodeID);
        Lock writeLock = tx.acquireWriteLock(node);
        node.delete();
        writeLock.release();
        sendOK();
        return null;
    }

}
