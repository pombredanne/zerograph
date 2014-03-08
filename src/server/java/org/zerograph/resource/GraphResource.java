package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.Graph;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zerograph.except.ServiceAlreadyStartedException;
import org.zerograph.except.ServiceNotStartedException;
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
        Graph graph = Graph.getInstance(getZerograph(), host, port);
        sendOK(graph);
        return null;
    }

    /**
     * PUT graph {host} {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer put(Request request) throws ClientError, ServerError {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        // TODO: get created flag
        try {
            Graph graph = Graph.startInstance(getZerograph(), host, port, true);
            sendOK(graph);  // TODO: might be 201 Created instead
        } catch (ServiceAlreadyStartedException ex) {
            Graph graph = Graph.getInstance(getZerograph(), host, port);
            sendOK(graph);
        }
        return null;
    }

    /**
     * DELETE graph {host} {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer delete(Request request) throws ClientError, ServerError {
        String host = request.getStringData(0);
        int port = request.getIntegerData(1);
        // TODO: get created flag
        try {
            Graph.stopInstance(getZerograph(), host, port, false);
            sendOK();
        } catch (ServiceNotStartedException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "No graph on port " + port));
        }
        return null;
    }

}
