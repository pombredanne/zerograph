package org.zerograph.zapp.resources;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.MethodNotAllowed;
import org.zerograph.zapp.except.ServerError;


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

    protected Node resolveNode(DatabaseInterface context, Object value) throws ClientError {
        if (value == null) {
            return null;
        } else if (value instanceof Node) {
            return (Node)value;
        } else if (value instanceof Integer) {
            try {
                return context.getNode((Integer) value);
            } catch (NotFoundException ex) {
                throw new ClientError("Relationship " + value + " not found");
            }
        } else if (value instanceof Long) {
            try {
                return context.getNode((Long) value);
            } catch (NotFoundException ex) {
                throw new ClientError("Relationship " + value + " not found");
            }
        } else {
            throw new ClientError("Cannot resolve relationship " + value);
        }
    }

}