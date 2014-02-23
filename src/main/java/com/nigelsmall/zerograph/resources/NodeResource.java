package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

public class NodeResource extends Resource {

    final public static String NAME = "node";

    public NodeResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * GET node <db_name> <node_id>
     *
     * @param request
     */
    @Override
    public void get(Request request) {
        String databaseName;
        Long nodeID;

        try {
            databaseName = getStringArgument(request, 0);
            nodeID = getLongArgument(request, 1);
        } catch (BadRequest ex) {
            send(Response.BAD_REQUEST, ex.getMessage());
            return;
        }

        GraphDatabaseService database = environment().getDatabase(databaseName);

        try (Transaction tx = database.beginTx()) {
            try {
                send(Response.OK, new Object[] { database.getNodeById(nodeID) });
            } catch (NotFoundException ex) {
                send(Response.NOT_FOUND);
            }
        }

    }

}
