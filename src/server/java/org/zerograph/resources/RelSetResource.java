package org.zerograph.resources;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;

public class RelSetResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "RelSet";

    public RelSetResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET RelSet {"start": …}
     * GET RelSet {"end": …}
     * GET RelSet {"start": …, "end": …}
     * GET RelSet {"start": …, "type": …}
     * GET RelSet {"end": …, "type": …}
     * GET RelSet {"start": …, "end": …, "type": …}
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
     * PATCH RelSet {"start": …, "end": …, "type": …}
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
     * DELETE RelSet {"start": …}
     * DELETE RelSet {"end": …}
     * DELETE RelSet {"start": …, "end": …}
     * DELETE RelSet {"start": …, "type": …}
     * DELETE RelSet {"end": …, "type": …}
     * DELETE RelSet {"start": …, "end": …, "type": …}
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
