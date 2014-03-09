package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.Graph;
import org.zerograph.GraphDirectory;
import org.zerograph.Request;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.Conflict;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zerograph.except.NotFound;
import org.zerograph.except.ServerError;
import org.zeromq.ZMQ;

public class GraphResource extends BaseZerographResource {

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
    public PropertyContainer get(Request request) throws ClientError, ServerError {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        GraphDirectory directory = new GraphDirectory(getZerograph(), host, port);
        if (directory.exists()) {
            sendOK(directory);  // check if started
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
    public PropertyContainer put(Request request) throws ClientError, ServerError {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        boolean create = request.getBooleanData(2, false);
        GraphDirectory directory = new GraphDirectory(getZerograph(), host, port);
        if (directory.exists() || create) {
            try {
                Graph graph = Graph.startInstance(getZerograph(), host, port, create);
                sendOK(graph);
            } catch (GraphAlreadyStartedException ex) {
                throw new Conflict("Unable to start graph on port " + port);
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
    public PropertyContainer delete(Request request) throws ClientError, ServerError {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        boolean delete = request.getBooleanData(2, false);
        // TODO: get deleted flag
        try {
            Graph.stopInstance(getZerograph(), host, port, false);
            sendOK();
        } catch (GraphNotStartedException ex) {
            throw new NotFound("No graph on port " + port);
        }
        return null;
    }

}
