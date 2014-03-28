package org.zerograph.zap;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.MethodNotAllowed;
import org.zerograph.zpp.except.ServerError;


public abstract class AbstractResource implements ResourceInterface {

    final protected ResponderInterface responder;

    public AbstractResource(ResponderInterface responder) {
        this.responder = responder;
    }

    public PropertyContainer get(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer set(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer patch(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer create(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer delete(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer execute(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

}
