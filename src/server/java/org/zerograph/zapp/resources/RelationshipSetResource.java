package org.zerograph.zapp.resources;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.ServerError;

public class RelationshipSetResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "RelSet";

    public RelationshipSetResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET RelSet {"start": 1}
     * GET RelSet {"end": 2}
     * GET RelSet {"start": 1, "end": 2}
     * GET RelSet {"start": 1, "type": "KNOWS"}
     * GET RelSet {"type": "KNOWS", "end": 2}
     * GET RelSet {"start": 1, "type": "KNOWS", "end": 2}
     *
     */
    @Override
    public PropertyContainer get(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        Node startNode = resolveNode(context, request.getArgument("start", null));
        Node endNode = resolveNode(context, request.getArgument("end", null));
        String type = request.getArgumentAsString("type", null);
        Iterable<Relationship> result = context.matchRelationshipSet(startNode, endNode, type);
        return responder.sendRelationships(result);
    }

    /**
     * PATCH RelSet {"start":1, "type": "KNOWS", "end": 2}
     *
     * Ensure at least one relationship exists with the specified criteria.
     *
     */
    @Override
    public PropertyContainer patch(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        Node startNode = resolveNode(context, request.getArgument("start"));
        Node endNode = resolveNode(context, request.getArgument("end"));
        String type = request.getArgumentAsString("type");
        Iterable<Relationship> result = context.mergeRelationshipSet(startNode, endNode, type);
        return responder.sendRelationships(result);
    }

    /**
     * DELETE RelSet {"start": 1}
     * DELETE RelSet {"end": 2}
     * DELETE RelSet {"start": 1, "end": 2}
     * DELETE RelSet {"start": 1, "type": "KNOWS"}
     * DELETE RelSet {"type": "KNOWS", "end": 2}
     * DELETE RelSet {"start": 1, "type": "KNOWS", "end": 2}
     *
     */
    @Override
    public PropertyContainer delete(RequestInterface request, DatabaseInterface context) throws ClientError, ServerError {
        Node startNode = resolveNode(context, request.getArgument("start", null));
        Node endNode = resolveNode(context, request.getArgument("end", null));
        String type = request.getArgumentAsString("type", null);
        Iterable<Relationship> result = context.purgeRelationshipSet(startNode, endNode, type);
        return responder.sendRelationships(result);
    }

}
