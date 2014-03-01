package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

public class RelResource extends PropertyContainerResource {

    final public static String NAME = "rel";

    public RelResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * GET rel {rel_id}
     *
     * Fetch a single relationship by ID.
     */
    @Override
    public void get(Transaction transaction, Request request) throws ClientError, ServerError {
        long relID = getArgument(request, 0, Integer.class);
        try {
            Relationship rel = database().getRelationshipById(relID);
            sendOK(rel);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, relID));
        }
    }

    /**
     * PUT rel {rel_id} {properties}
     *
     * Replace all properties on a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist.
     */
    @Override
    public void put(Transaction transaction, Request request) throws ClientError, ServerError {
        long relID = getArgument(request, 0, Integer.class);
        Map properties = getArgument(request, 1, Map.class);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = transaction.acquireWriteLock(rel);
            Lock readLock = transaction.acquireReadLock(rel);
            removeProperties(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            sendOK(rel);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, relID));
        }
    }

    /**
     * PATCH rel {rel_id} {properties}
     *
     * Add new properties to a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist and any existing properties will be
     * maintained.
     */
    @Override
    public void patch(Transaction transaction, Request request) throws ClientError, ServerError {
        long relID = getArgument(request, 0, Integer.class);
        Map properties = getArgument(request, 1, Map.class);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = transaction.acquireWriteLock(rel);
            Lock readLock = transaction.acquireReadLock(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            sendOK(rel);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, relID));
        }
    }

    /**
     * POST rel {start_node_id} {end_node_id} {type} {properties}
     *
     * Create a new relationship.
     */
    @Override
    public void post(Transaction transaction, Request request) throws ClientError, ServerError {
        long startNodeID = getArgument(request, 0, Integer.class);
        long endNodeID = getArgument(request, 1, Integer.class);
        String typeName = getArgument(request, 2, String.class);
        Map properties = getArgument(request, 3, Map.class);
        Node startNode;
        Node endNode;
        try {
            startNode = database().getNodeById(startNodeID);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, startNodeID));
        }
        try {
            endNode = database().getNodeById(endNodeID);
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, endNodeID));
        }
        Relationship rel = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName(typeName));
        Lock writeLock = transaction.acquireWriteLock(rel);
        Lock readLock = transaction.acquireReadLock(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        sendOK(rel);
    }

    /**
     * DELETE rel {rel_id}
     *
     * Delete a relationship identified by ID.
     */
    @Override
    public void delete(Transaction transaction, Request request) throws ClientError, ServerError {
        long relID = getArgument(request, 0, Integer.class);
        Relationship rel = database().getRelationshipById(relID);
        Lock writeLock = transaction.acquireWriteLock(rel);
        rel.delete();
        writeLock.release();
        sendOK();
    }

}
