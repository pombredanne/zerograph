package org.zerograph.zap;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zerograph.neo4j.api.DatabaseInterface;
import org.zerograph.service.GraphDirectory;
import org.zerograph.service.api.GraphInterface;
import org.zerograph.service.api.ZerographInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

public class GraphMapResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "GraphMap";

    public GraphMapResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
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
        GraphDirectory directory = new GraphDirectory(zerograph, host, port);
        if (directory.exists()) {
            responder.sendBodyPart(directory);  // check if started
        } else {
            throw new ClientError("No graph directory exists for " + host + ":" + port);
        }
        return null;
    }

    /**
     * CREATE graph {host} {port} [{create}]
     *
     * @param request
     */
    @Override
    public PropertyContainer create(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        boolean create = request.getArgumentAsBoolean("create", false);
        GraphDirectory directory = new GraphDirectory(zerograph, host, port);
        if (directory.exists() || create) {
            try {
                GraphInterface graph = zerograph.startGraph(host, port, create);
                responder.sendBodyPart(graph);
            } catch (GraphAlreadyStartedException ex) {
                responder.sendBodyPart(ex.getGraph());
            } catch (NoSuchGraphException ex) {
                throw new ClientError("No graph exists for port " + port);
            }
        } else {
            throw new ClientError("No graph directory exists for " + host + ":" + port);
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
            zerograph.stopGraph(host, port, false);
        } catch (GraphNotStartedException ex) {
            throw new ClientError("No graph on port " + port);
        }
        return null;
    }

}
