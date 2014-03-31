package org.zerograph.zap;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

import java.util.Map;

public class RelationshipResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Rel";

    public RelationshipResource(ResponderInterface responder) {
        super(responder);
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
    public PropertyContainer get(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        try {
            Relationship rel = context.getRelationship(id);
            responder.sendBody(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError("Relationship " + id + " not found");
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
    public PropertyContainer set(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        Map<String, Object> properties = request.getArgumentAsMap("properties");
        try {
            Relationship rel = context.putRelationship(id, properties);
            responder.sendBody(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError("Relationship " + id + " not found");
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
    public PropertyContainer patch(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        Map<String, Object> properties = request.getArgumentAsMap("properties");
        try {
            Relationship rel = context.patchRelationship(id, properties);
            responder.sendBody(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError("Relationship " + id + " not found");
        }
    }

    /**
     * POST rel {start_node} {end_node} {type} {properties}
     *
     * Create a new relationship.
     */
    @Override
    public PropertyContainer create(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        Node startNode = resolveNode(context, request.getArgument("start"));
        Node endNode = resolveNode(context, request.getArgument("end"));
        String typeName = request.getArgumentAsString("type");
        Map<String, Object> properties = request.getArgumentAsMap("properties");
        Relationship rel = context.createRelationship(startNode, endNode, typeName, properties);
        responder.sendBody(rel);
        return rel;
    }

    /**
     * DELETE rel {rel_id}
     *
     * Delete a relationship identified by ID.
     */
    @Override
    public PropertyContainer delete(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        long id = request.getArgumentAsLong("id");
        try {
            context.deleteRelationship(id);
            return null;
        } catch (NotFoundException ex) {
            throw new ClientError("Relationship " + id + " not found");
        }
    }

    private Node resolveNode(DatabaseInterface context, Object value) throws ClientError {
        if (value instanceof Node) {
            return (Node)value;
        } else if (value instanceof Integer) {
            try {
                return context.getNode((Integer) value);
            } catch (NotFoundException ex) {
                throw new ClientError("Relationship " + value + " not found");
            }
        } else if (value instanceof Long) {
            try {
                return context.getNode((Long) value);
            } catch (NotFoundException ex) {
                throw new ClientError("Relationship " + value + " not found");
            }
        } else {
            throw new ClientError("Cannot resolve relationship " + value);
        }
    }

}
