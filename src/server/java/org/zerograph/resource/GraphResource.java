package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.GraphDirectory;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.GraphInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public class GraphResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "graph";

    public GraphResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET graph {host} {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer get(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        String host = request.getStringData(0);
        int port = new Long(request.getLongData(1)).intValue();
        GraphDirectory directory = new GraphDirectory(zerograph, host, port);
        if (directory.exists()) {
            respond(new OK(directory));  // check if started
        } else {
            throw new NotFound("No graph directory exists for " + host + ":" + port);
        }
        return null;
    }

    /**
     * PUT graph {host} {port} [{create}]
     *
     * @param request
     */
    @Override
    public PropertyContainer put(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        String host = request.getStringData(0);
        int port = new Long(request.getLongData(1)).intValue();
        boolean create = request.getBooleanData(2, false);
        GraphDirectory directory = new GraphDirectory(zerograph, host, port);
        if (directory.exists() || create) {
            try {
                GraphInterface graph = zerograph.startGraph(host, port, create);
                respond(new OK(graph));
            } catch (GraphAlreadyStartedException ex) {
                respond(new OK(ex.getGraph()));
            } catch (NoSuchGraphException ex) {
                throw new NotFound("No graph exists for port " + port);
            }
        } else {
            throw new NotFound("No graph directory exists for " + host + ":" + port);
        }
        return null;
    }

    /**
     * DELETE graph {host} {port} [{delete}]
     *
     * @param request
     */
    @Override
    public PropertyContainer delete(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        String host = request.getStringData(0);
        int port = new Long(request.getLongData(1)).intValue();
        boolean delete = request.getBooleanData(2, false);
        // TODO: get deleted flag
        try {
            zerograph.stopGraph(host, port, false);
            respond(new OK());
        } catch (GraphNotStartedException ex) {
            throw new NotFound("No graph on port " + port);
        }
        return null;
    }

}
