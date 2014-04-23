package org.zerograph.resources;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;

import java.util.Map;

public class RelResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Rel";

    public RelResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET Rel {"id": …}
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
            throw new ClientError("Relationship " + id + " not found!");
        }
    }

    /**
     * SET Rel {"id": …, "properties": …}
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
     * PATCH Rel {"id": …, "properties": …}
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
     * CREATE Rel {"start": …, "end": …, "type": …, "properties": …}
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
     * DELETE Rel {"id": …}
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

}
