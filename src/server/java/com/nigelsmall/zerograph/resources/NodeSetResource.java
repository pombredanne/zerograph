package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeSetResource extends Resource {

    final public static String NAME = "nodeset";

    public NodeSetResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * GET nodeset {db} {label} {key} {value}
     *
     * MATCH-RETURN
     *
     * @param request
     */
    @Override
    public void get(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            Label label = DynamicLabel.label(getArgument(request, 1, String.class));
            String key = getArgument(request, 2, String.class);
            Object value = getArgument(request, 3, Object.class);
            try (Transaction tx = database.beginTx()) {
                HashMap<String, Integer> stats = new HashMap<>();
                stats.put("matched", 0);
                for (Node node : database.findNodesByLabelAndProperty(label, key, value)) {
                    send(new Response(Response.CONTINUE, node));
                    stats.put("matched", stats.get("matched") + 1);
                }
                response = new Response(Response.OK, stats);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } finally {
            send(response);
        }
    }

    /**
     * PUT nodeset {db} {label} {key} {value}
     *
     * MERGE-RETURN
     *
     * @param request
     */
    @Override
    public void put(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            String labelName = getArgument(request, 1, String.class);
            String key = getArgument(request, 2, String.class);
            Object value = getArgument(request, 3, Object.class);
            //
            String query = "MERGE (a:`" + labelName.replace("`", "``") +
                    "` {`" + key.replace("`", "``") + "`:{value}}) RETURN a";
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("value", value);
            //
            try (Transaction tx = database.beginTx()) {
                ExecutionEngine engine = new ExecutionEngine(database);
                ExecutionResult result;
                result = engine.execute(query, params);
                for (Map<String, Object> row : result) {
                    send(new Response(Response.CONTINUE, row.get("a")));
                }
                tx.success();
                response = new Response(Response.OK);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (CypherException ex) {
            response = new Response(Response.BAD_REQUEST, ex.getMessage());
        } catch (TransactionFailureException ex) {
            response = new Response(Response.CONFLICT, ex.getMessage());  // TODO - derive cause from nested Exceptions
        } finally {
            send(response);
        }
    }

    /**
     * DELETE nodeset {db} {label} {key} {value}
     *
     * MATCH-DELETE
     *
     * @param request
     */
    @Override
    public void delete(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            Label label = DynamicLabel.label(getArgument(request, 1, String.class));
            String key = getArgument(request, 2, String.class);
            Object value = getArgument(request, 3, Object.class);
            try (Transaction tx = database.beginTx()) {
                HashMap<String, Integer> stats = new HashMap<>();
                stats.put("deleted", 0);
                for (Node node : database.findNodesByLabelAndProperty(label, key, value)) {
                    node.delete();
                    stats.put("deleted", stats.get("deleted") + 1);
                }
                tx.success();
                response = new Response(Response.OK, stats);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (TransactionFailureException ex) {
            response = new Response(Response.CONFLICT, ex.getMessage());  // TODO - derive cause from nested Exceptions
        } finally {
            send(response);
        }
    }

}
