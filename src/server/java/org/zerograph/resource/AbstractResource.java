package org.zerograph.resource;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;


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

    public PropertyContainer get(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer put(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer patch(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer post(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer delete(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void respond(ResponseInterface response) {
        responder.respond(response);
    }

}
