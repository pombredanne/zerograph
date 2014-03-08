package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zeromq.ZMQ;

/**
 * Base class for all resources used by the Zerograph.
 *
 */
public abstract class BaseZerographResource extends BaseResource {

    public BaseZerographResource(Zerograph zerograph, ZMQ.Socket socket) {
        super(zerograph, socket);
    }

    public PropertyContainer handle(Request request) throws ClientError, ServerError {
        switch (request.getMethod()) {
            case "GET":
                return get(request);
            case "PUT":
                return put(request);
            case "PATCH":
                return patch(request);
            case "POST":
                return post(request);
            case "DELETE":
                return delete(request);
            default:
                throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
        }
    }

    public PropertyContainer get(Request request) throws ClientError, ServerError {
        throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public PropertyContainer put(Request request) throws ClientError, ServerError {
        throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public PropertyContainer patch(Request request) throws ClientError, ServerError {
        throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public PropertyContainer post(Request request) throws ClientError, ServerError {
        throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public PropertyContainer delete(Request request) throws ClientError, ServerError {
        throw new ClientError(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

}
