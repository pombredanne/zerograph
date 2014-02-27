package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class NodeSetResource extends Resource {

    final public static String NAME = "nodeset";

    public NodeSetResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * GET nodeset {label} {key} {value}
     *
     * tx.find(label, key, value)
     *
     * MATCH-RETURN
     * No locking
     *
     * @param request
     */
    @Override
    public void get(Request request) throws ClientError {
        Label label = DynamicLabel.label(getArgument(request, 0, String.class));
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        try (Transaction tx = database().beginTx()) {
            HashMap<String, Integer> stats = new HashMap<>();
            stats.put("nodes_matched", 0);
            for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
                send(new Response(Response.CONTINUE, node));
                stats.put("nodes_matched", stats.get("nodes_matched") + 1);
            }
            send(new Response(Response.OK, stats));
        }
    }

    /**
     * PUT nodeset {label} {key} {value}
     *
     * tx.merge(label, key, value)
     *
     * MERGE-RETURN
     * No locking(?)
     *
     * @param request
     */
    @Override
    public void put(Request request) throws ClientError {
        String labelName = getArgument(request, 0, String.class);
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        try (Transaction tx = database().beginTx()) {
            HashMap<String, Integer> stats = new HashMap<>();
            String query = "MERGE (a:`" + labelName.replace("`", "``") +
                    "` {`" + key.replace("`", "``") + "`:{value}}) RETURN a";
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("value", value);
            ExecutionResult result = execute(query, params);
            for (Map<String, Object> row : result) {
                send(new Response(Response.CONTINUE, row.get("a")));
            }
            stats.put("nodes_created", result.getQueryStatistics().getNodesCreated());
            tx.success();
            send(new Response(Response.OK, stats));
        } catch (CypherException ex) {
            send(new Response(Response.SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * DELETE nodeset {label} {key} {value}
     *
     * tx.purge(label. key, value)
     *
     * MATCH-DELETE
     *
     * @param request
     */
    @Override
    public void delete(Request request) throws ClientError {
        Label label = DynamicLabel.label(getArgument(request, 0, String.class));
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        try (Transaction tx = database().beginTx()) {
            HashMap<String, Integer> stats = new HashMap<>();
            stats.put("nodes_deleted", 0);
            for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
                node.delete();
                stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
            }
            tx.success();
            send(new Response(Response.OK, stats));
        }
    }

}
