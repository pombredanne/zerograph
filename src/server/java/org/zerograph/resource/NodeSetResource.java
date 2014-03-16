package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.IterableResult;
import org.zerograph.Statistics;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status1xx.Continue;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.ServerError;
import org.zerograph.response.status5xx.Status5xx;

import java.util.HashMap;

public class NodeSetResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "nodeset";

    public NodeSetResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
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
    public PropertyContainer get(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        String label = request.getStringData(0);
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        IterableResult<Node> result = context.matchNodeSet(label, key, value);
        //stats.put("nodes_matched", 0);
        for (Node node : result) {
            respond(new Continue(node));
            //stats.put("nodes_matched", stats.get("nodes_matched") + 1);
        }
        respond(new OK(stats));
        return result.getFirst();
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
    public PropertyContainer put(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        String labelName = request.getStringData(0);
        String key = request.getStringData(1);
        Object value = request.getData(2);
        try {
            IterableResult<Node> result = context.mergeNodeSet(labelName, key, value);
            for (Node node : result) {
                respond(new Continue(node));
            }
            Statistics stats = result.getStatistics();
            if (stats.get("nodes_created") == 0) {
                respond(new OK(stats));
            } else {
                respond(new Created(stats));
            }
            return result.getFirst();
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
    public PropertyContainer delete(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        String label = request.getStringData(0);
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        //stats.put("nodes_deleted", 0);
        context.purgeNodeSet(label, key, value);
        //stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
        respond(new OK(stats));
        return null;
    }

}
