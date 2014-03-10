package org.zerograph.resource;

import org.zerograph.Request;
import org.zerograph.api.ResponseInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status5xx.Abstract5xx;
import org.zerograph.util.Data;
import org.zeromq.ZMQ;

import java.io.IOException;


public abstract class AbstractResource {

    final public static String NAME = null;

    final private ZerographInterface zerograph;
    final private ZMQ.Socket socket;

    public AbstractResource(ZerographInterface zerograph, ZMQ.Socket socket) {
        this.zerograph = zerograph;
        this.socket = socket;
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
    }

    public void get(Request request) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void put(Request request) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void patch(Request request) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void post(Request request) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void delete(Request request) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public void send(ResponseInterface response) {
        StringBuilder builder = new StringBuilder(Integer.toString(response.getStatus()));
        for (Object datum : response.getData()) {
            builder.append('\t');
            try {
                builder.append(Data.encode(datum));
            } catch (IOException ex) {
                builder.append('?');  // TODO
            }
        }
        String string = builder.toString();
        System.out.println(">>> " + string);
        socket.sendMore(string);
    }

}
