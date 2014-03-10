package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.Graph;
import org.zerograph.GraphDirectory;
import org.zerograph.Request;
import org.zerograph.Zerograph;
import org.zerograph.api.GlobalResourceInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.Abstract5xx;
import org.zeromq.ZMQ;

public class GraphResource extends AbstractResource implements GlobalResourceInterface {

    final public static String NAME = "graph";

    public GraphResource(Zerograph zerograph, ZMQ.Socket socket) {
        super(zerograph, socket);
    }

    /**
     * GET graph {host} {port}
     *
     * @param request
     */
    @Override
    public void get(Request request) throws Abstract4xx, Abstract5xx {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        GraphDirectory directory = new GraphDirectory(getZerograph(), host, port);
        if (directory.exists()) {
            send(new OK(directory));  // check if started
        } else {
            throw new NotFound("No graph directory exists for " + host + ":" + port);
        }
    }

    /**
     * PUT graph {host} {port} [{create}]
     *
     * @param request
     */
    @Override
    public void put(Request request) throws Abstract4xx, Abstract5xx {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        boolean create = request.getBooleanData(2, false);
        GraphDirectory directory = new GraphDirectory(getZerograph(), host, port);
        if (directory.exists() || create) {
            try {
                Graph graph = Graph.startInstance(getZerograph(), host, port, create);
                send(new OK(graph));
            } catch (GraphAlreadyStartedException ex) {
                throw new Conflict("Unable to start graph on port " + port);
            } catch (NoSuchGraphException ex) {
                throw new NotFound("No graph exists for port " + port);
            }
        } else {
            throw new NotFound("No graph directory exists for " + host + ":" + port);
        }
    }

    /**
     * DELETE graph {host} {port} [{delete}]
     *
     * @param request
     */
    @Override
    public void delete(Request request) throws Abstract4xx, Abstract5xx {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        boolean delete = request.getBooleanData(2, false);
        // TODO: get deleted flag
        try {
            Graph.stopInstance(getZerograph(), host, port, false);
            send(new OK());
        } catch (GraphNotStartedException ex) {
            throw new NotFound("No graph on port " + port);
        }
    }

}
