package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status1xx.Continue;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.ServerError;
import org.zerograph.response.status5xx.Status5xx;

import java.util.HashMap;
import java.util.Map;

public class NodeSetResource extends AbstractTransactionalResource implements TransactionalResourceInterface {

    final private static String NAME = "nodeset";

    public NodeSetResource(ZerographInterface zerograph, ResponderInterface responder, GraphDatabaseService database) {
        super(zerograph, responder, database);
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
    public PropertyContainer get(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        Label label = DynamicLabel.label(request.getStringData(0));
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_matched", 0);
        Node firstNode = null;
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            respond(new Continue(node));
            if (firstNode == null) {
                firstNode = node;
            }
            stats.put("nodes_matched", stats.get("nodes_matched") + 1);
        }
        respond(new OK(stats));
        return firstNode;
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
    public PropertyContainer put(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        String labelName = request.getStringData(0);
        String key = request.getStringData(1);
        Object value = request.getData(2);
        try {
            HashMap<String, Integer> stats = new HashMap<>();
            String query = "MERGE (a:`" + labelName.replace("`", "``") +
                    "` {`" + key.replace("`", "``") + "`:{value}}) RETURN a";
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("value", value);
            ExecutionResult result = execute(query, params);
            Node firstNode = null;
            for (Map<String, Object> row : result) {
                Node node = (Node)row.get("a");
                respond(new Continue(node));
                if (firstNode == null) {
                    firstNode = node;
                }
            }
            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            stats.put("nodes_created", nodesCreated);
            if (nodesCreated == 0) {
                respond(new OK(stats));
            } else {
                respond(new Created(stats));
            }
            return firstNode;
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
    public PropertyContainer delete(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        Label label = DynamicLabel.label(request.getStringData(0));
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_deleted", 0);
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            node.delete();
            stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
        }
        respond(new OK(stats));
        return null;
    }

}
