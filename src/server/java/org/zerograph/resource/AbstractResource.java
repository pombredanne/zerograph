package org.zerograph.resource;

import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;
import org.zerograph.util.Data;
import org.zeromq.ZMQ;

import java.io.IOException;


public abstract class AbstractResource {

    final private ZerographInterface zerograph;
    final private ResponderInterface responder;

    public AbstractResource(ZerographInterface zerograph, ResponderInterface responder) {
        this.zerograph = zerograph;
        this.responder = responder;
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
    }

    public void get(RequestInterface request) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void put(RequestInterface request) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void patch(RequestInterface request) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void post(RequestInterface request) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void delete(RequestInterface request) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void respond(ResponseInterface response) {
        responder.respond(response);
    }

}
