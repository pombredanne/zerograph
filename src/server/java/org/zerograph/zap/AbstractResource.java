package org.zerograph.zap;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.service.api.ZerographInterface;
import org.zerograph.neo4j.api.DatabaseInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.MethodNotAllowed;
import org.zerograph.zpp.except.ServerError;


public abstract class AbstractResource implements ResourceInterface {

    final protected ZerographInterface zerograph;
    final protected ResponderInterface responder;

    public AbstractResource(ZerographInterface zerograph, ResponderInterface responder) {
        this.zerograph = zerograph;
        this.responder = responder;
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
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
