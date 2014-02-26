package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

import static com.nigelsmall.zerograph.util.Helpers.*;

public class NodeResource extends Resource {

    final public static String NAME = "node";

    public NodeResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * GET node {db} {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public void get(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            long nodeID = getArgument(request, 1, Integer.class);
            try (Transaction tx = database.beginTx()) {
                Node node = database.getNodeById(nodeID);
                response = new Response(Response.OK, node);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (NotFoundException ex) {
            response = new Response(Response.NOT_FOUND);
        } finally {
            send(response);
        }
    }

    /**
     * PUT node {db} {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public void put(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            long nodeID = getArgument(request, 1, Integer.class);
            List labelNames = getArgument(request, 2, List.class);
            Map properties = getArgument(request, 3, Map.class);
            try (Transaction tx = database.beginTx()) {
                Node node = database.getNodeById(nodeID);
                tx.acquireWriteLock(node);
                tx.acquireReadLock(node);
                removeLabels(node);
                removeProperties(node);
                addLabels(node, labelNames);
                addProperties(node, properties);
                tx.success();
                response = new Response(Response.OK, node);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (NotFoundException ex) {
            response = new Response(Response.NOT_FOUND);
        } finally {
            send(response);
        }
    }

    /**
     * PATCH node {db} {node_id} {labels} {properties}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public void patch(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            long nodeID = getArgument(request, 1, Integer.class);
            List labelNames = getArgument(request, 2, List.class);
            Map properties = getArgument(request, 3, Map.class);
            try (Transaction tx = database.beginTx()) {
                Node node = database.getNodeById(nodeID);
                tx.acquireWriteLock(node);
                tx.acquireReadLock(node);
                addLabels(node, labelNames);
                addProperties(node, properties);
                tx.success();
                response = new Response(Response.OK, node);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (NotFoundException ex) {
            response = new Response(Response.NOT_FOUND);
        } finally {
            send(response);
        }
    }

    /**
     * POST node {db} {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public void post(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            List labelNames = getArgument(request, 1, List.class);
            Map properties = getArgument(request, 2, Map.class);
            try (Transaction tx = database.beginTx()) {
                Node node = database.createNode();
                tx.acquireWriteLock(node);
                tx.acquireReadLock(node);
                addLabels(node, labelNames);
                addProperties(node, properties);
                tx.success();
                response = new Response(Response.OK, node);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } finally {
            send(response);
        }
    }

    /**
     * DELETE node {db} {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public void delete(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            long nodeID = getArgument(request, 1, Integer.class);
            try (Transaction tx = database.beginTx()) {
                Node node = database.getNodeById(nodeID);
                tx.acquireWriteLock(node);
                tx.acquireReadLock(node);
                node.delete();
                tx.success();
                response = new Response(Response.NO_CONTENT);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (NotFoundException ex) {
            response = new Response(Response.NOT_FOUND);
        } finally {
            send(response);
        }
    }

}
