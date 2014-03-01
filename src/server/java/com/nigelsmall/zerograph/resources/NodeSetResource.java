package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
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
    public void get(Transaction transaction, Request request) throws ClientError, ServerError {
        Label label = DynamicLabel.label(getArgument(request, 0, String.class));
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_matched", 0);
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            sendContinue(node);
            stats.put("nodes_matched", stats.get("nodes_matched") + 1);
        }
        sendOK(stats);
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
    public void put(Transaction transaction, Request request) throws ClientError, ServerError {
        String labelName = getArgument(request, 0, String.class);
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        try {
            HashMap<String, Integer> stats = new HashMap<>();
            String query = "MERGE (a:`" + labelName.replace("`", "``") +
                    "` {`" + key.replace("`", "``") + "`:{value}}) RETURN a";
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("value", value);
            ExecutionResult result = execute(query, params);
            for (Map<String, Object> row : result) {
                sendContinue(row.get("a"));
            }
            stats.put("nodes_created", result.getQueryStatistics().getNodesCreated());
            sendOK(stats);
        } catch (CypherException ex) {
            throw new ServerError(new Response(Response.SERVER_ERROR, ex.getMessage()));
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
    public void delete(Transaction transaction, Request request) throws ClientError, ServerError {
        Label label = DynamicLabel.label(getArgument(request, 0, String.class));
        String key = getArgument(request, 1, String.class);
        Object value = getArgument(request, 2, Object.class);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_deleted", 0);
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            node.delete();
            stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
        }
        sendOK(stats);
    }

}
