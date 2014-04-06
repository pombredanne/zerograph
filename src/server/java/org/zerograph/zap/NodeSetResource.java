package org.zerograph.zap;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

import java.util.HashMap;

public class NodeSetResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "NodeSet";

    public NodeSetResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
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
    public PropertyContainer get(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        String labelName = request.getArgumentAsString("label");
        String key = request.getArgumentAsString("key", null);
        Object value = request.getArgument("value", null);
        HashMap<String, Object> stats = new HashMap<>();
        Iterable<Node> result = context.matchNodeSet(labelName, key, value);
        Node first = null;
        //stats.put("nodes_matched", 0);
        responder.startBodyList();
        for (Node node : result) {
            if (first == null) {
                first = node;
            }
            responder.sendBodyItem(node);
            //stats.put("nodes_matched", stats.get("nodes_matched") + 1);
        }
        responder.endBodyList();
        responder.sendFoot(stats);
        return first;
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
    public PropertyContainer set(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        String labelName = request.getArgumentAsString("label");
        String key = request.getArgumentAsString("key");
        Object value = request.getArgument("value");
        try {
            Iterable<Node> result = context.mergeNodeSet(labelName, key, value);
            Node first = null;
            responder.startBodyList();
            for (Node node : result) {
                if (first == null) {
                    first = node;
                }
                responder.sendBodyItem(node);
            }
            responder.endBodyList();
            //Statistics stats = result.getStatistics();
            HashMap<String, Object> stats = new HashMap<>();
            responder.sendFoot(stats);
            return first;
        } catch (CypherException ex) {
            throw new ServerError(ex.getMessage());
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
    public PropertyContainer delete(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        String labelName = request.getArgumentAsString("label");
        String key = request.getArgumentAsString("key");
        Object value = request.getArgument("value");
        HashMap<String, Object> stats = new HashMap<>();
        //stats.put("nodes_deleted", 0);
        context.purgeNodeSet(labelName, key, value);
        responder.startBodyList();
        responder.endBodyList();
        //stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
        responder.sendFoot(stats);
        return null;
    }

}
