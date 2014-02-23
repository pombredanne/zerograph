package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

public class NodeResource extends Resource {

    final public static String NAME = "node";

    public NodeResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * GET node {db} {node_id}
     *
     * @param request
     */
    @Override
    public void get(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            long nodeID = getIntegerArgument(request, 1);
            try (Transaction tx = database.beginTx()) {
                try {
                    response = new Response(Response.OK, database.getNodeById(nodeID));
                } catch (NotFoundException ex) {
                    response = new Response(Response.NOT_FOUND);
                }
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } finally {
            send(response);
        }
    }

    /**
     * PUT node {db} {node_id} {labels} {properties}
     *
     * @param request
     */
    @Override
    public void put(Request request) {
        send(new Response(Response.NOT_IMPLEMENTED));
    }

    /**
     * PATCH node {db} {node_id} {labels} {properties}
     *
     * @param request
     */
    @Override
    public void patch(Request request) {
        send(new Response(Response.NOT_IMPLEMENTED));
    }

    /**
     * POST node {db} {labels} {properties}
     *
     * @param request
     */
    @Override
    public void post(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            List labelNames = getListArgument(request, 1);
            Map properties = getMapArgument(request, 2);
            try (Transaction tx = database.beginTx()) {
                Label[] labels = new Label[labelNames.size()];
                for (int i = 0; i < labels.length; i++)
                    labels[i] = DynamicLabel.label(labelNames.get(i).toString());
                Node node = database.createNode(labels);
                for (Object key : properties.keySet()) {
                    node.setProperty(key.toString(), properties.get(key));
                }
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
     * @param request
     */
    @Override
    public void delete(Request request) {
        send(new Response(Response.NOT_IMPLEMENTED));
    }

}
