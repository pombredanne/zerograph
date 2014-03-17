package org.zerograph.resource;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.NoContent;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

import java.util.Map;

public class RelResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "rel";

    public RelResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET rel {rel_id}
     *
     * Fetch a single relationship by ID.
     */
    @Override
    public PropertyContainer get(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        long relID = request.getLongData(0);
        try {
            Relationship rel = context.getRelationship(relID);
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * PUT rel {rel_id} {properties}
     *
     * Replace all properties on a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        long relID = request.getLongData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = context.putRelationship(relID, properties);
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * PATCH rel {rel_id} {properties}
     *
     * Add new properties to a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist and any existing properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        long relID = request.getLongData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = context.patchRelationship(relID, properties);
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * POST rel {start_node} {end_node} {type} {properties}
     *
     * Create a new relationship.
     */
    @Override
    public PropertyContainer post(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        Node startNode = resolveNode(context, request.getData(0));
        Node endNode = resolveNode(context, request.getData(1));
        String typeName = request.getStringData(2);
        Map properties = request.getMapData(3);
        Relationship rel = context.createRelationship(startNode, endNode, typeName, properties);
        respond(new Created(rel));
        return rel;
    }

    /**
     * DELETE rel {rel_id}
     *
     * Delete a relationship identified by ID.
     */
    @Override
    public PropertyContainer delete(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        long relID = request.getLongData(0);
        try {
            context.deleteRelationship(relID);
            respond(new NoContent());
            return null;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    private Node resolveNode(Neo4jContextInterface context, Object value) throws Status4xx {
        if (value instanceof Node) {
            return (Node)value;
        } else if (value instanceof Integer) {
            try {
                return context.getNode((Integer) value);
            } catch (NotFoundException ex) {
                throw new NotFound("Relationship " + value + " not found");
            }
        } else if (value instanceof Long) {
            try {
                return context.getNode((Long) value);
            } catch (NotFoundException ex) {
                throw new NotFound("Relationship " + value + " not found");
            }
        } else {
            throw new BadRequest(value);
        }
    }

}
