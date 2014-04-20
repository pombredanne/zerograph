package org.zerograph.zapp.resources;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.Graph;
import org.zerograph.GraphDirectory;
import org.zerograph.api.GraphInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.ServerError;

public class GraphResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Graph";

    public GraphResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET Graph {"host": …, "port": …}
     *
     */
    @Override
    public PropertyContainer get(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        GraphDirectory directory = new GraphDirectory(host, port);
        if (directory.exists()) {
            responder.sendBody(directory);  // check if started
        } else {
            throw new ClientError("No graph directory exists for " + host + ":" + port);
        }
        return null;
    }

    /**
     * PATCH Graph {"host": …, "port": …}
     *
     */
    @Override
    public PropertyContainer patch(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        try {
            GraphInterface graph = Graph.open(host, port);
            responder.sendBody(graph);
        } catch (GraphAlreadyStartedException ex) {
            responder.sendBody(ex.getGraph());
        }
        return null;
    }

    /**
     * DELETE Graph {"host": …, "port": …}
     *
     */
    @Override
    public PropertyContainer delete(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String host = request.getArgumentAsString("host");
        int port = request.getArgumentAsInteger("port");
        try {
            Graph.close(host, port, true);
        } catch (GraphNotStartedException ex) {
            throw new ClientError("No graph on port " + port);
        }
        return null;
    }

}
