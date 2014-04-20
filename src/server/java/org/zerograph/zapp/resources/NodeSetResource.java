package org.zerograph.zapp.resources;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.ServerError;

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
     * GET NodeSet {"label": …}
     * GET NodeSet {"label": …, "key": …, "value": …}
     *
     * Fetch all nodes that have the specified label and, optionally, property.
     *
     */
    @Override
    public PropertyContainer get(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        String labelName = request.getArgumentAsString("label");
        String key = request.getArgumentAsString("key", null);
        Object value = request.getArgument("value", null);
        HashMap<String, Object> stats = new HashMap<>();
        Iterable<Node> result = context.matchNodeSet(labelName, key, value);
        Node first = responder.sendNodes(result);
        responder.sendFoot(stats);
        return first;
    }

    /**
     * PATCH NodeSet {"label": …, "key": …, "value": …}
     *
     * Ensure at least one node exists with the specified criteria.
     *
     */
    @Override
    public PropertyContainer patch(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        String labelName = request.getArgumentAsString("label");
        String key = request.getArgumentAsString("key");
        Object value = request.getArgument("value");
        Iterable<Node> result = context.mergeNodeSet(labelName, key, value);
        Node first = responder.sendNodes(result);
        HashMap<String, Object> stats = new HashMap<>();
        responder.sendFoot(stats);
        return first;
    }

    /**
     * DELETE NodeSet {"label": …}
     * DELETE NodeSet {"label": …, "key": …, "value": …}
     *
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
