package org.zerograph.zap;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.Graph;
import org.zerograph.GraphDirectory;
import org.zerograph.api.GraphInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

public class GraphMapResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "GraphMap";

    public GraphMapResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET GraphMap {host} {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer get(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        GraphDirectory directory = new GraphDirectory(host, port);
        if (directory.exists()) {
            responder.sendBodyPart(directory);  // check if started
        } else {
            throw new ClientError("No graph directory exists for " + host + ":" + port);
        }
        return null;
    }

    /**
     * SET graph {host} {port} [{create}]
     *
     * @param request
     */
    @Override
    public PropertyContainer set(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        try {
            GraphInterface graph = Graph.setInstance(host, port);
            responder.sendBodyPart(graph);
        } catch (GraphAlreadyStartedException ex) {
            responder.sendBodyPart(ex.getGraph());
        }
        return null;
    }

    /**
     * DELETE graph {host} {port} [{delete}]
     *
     * @param request
     */
    @Override
    public PropertyContainer delete(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        boolean delete = request.getArgumentAsBoolean("delete", false);
        // TODO: get deleted flag
        try {
            Graph.stopInstance(host, port, false);
        } catch (GraphNotStartedException ex) {
            throw new ClientError("No graph on port " + port);
        }
        return null;
    }

}
