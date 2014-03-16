package org.zerograph.resource;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.NoContent;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

import java.util.List;
import java.util.Map;

public class NodeResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "node";

    public NodeResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public PropertyContainer get(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        long nodeID = request.getLongData(0);
        try {
            Node node = context.getNode(nodeID);
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * PUT node {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        long nodeID = request.getLongData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = context.putNode(nodeID, labelNames, properties);
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * PATCH node {node_id} {labels} {properties}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        long nodeID = request.getLongData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = context.patchNode(nodeID, labelNames, properties);
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * POST node {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public PropertyContainer post(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        List labelNames = request.getListData(0);
        Map properties = request.getMapData(1);
        Node node = context.createNode(labelNames, properties);
        respond(new Created(node));
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx {
        long nodeID = request.getLongData(0);
        try {
            context.deleteNode(nodeID);
            respond(new NoContent());
            return null;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

}
