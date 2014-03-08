package org.zerograph.resource;

import org.zerograph.Response;
import org.zerograph.Zerograph;
import org.zeromq.ZMQ;


public abstract class BaseResource {

    final public static String NAME = null;

    final private Zerograph zerograph;
    final private ZMQ.Socket socket;

    public BaseResource(Zerograph zerograph, ZMQ.Socket socket) {
        this.zerograph = zerograph;
        this.socket = socket;
    }

    public Zerograph getZerograph() {
        return this.zerograph;
    }

    private void send(Response response) {
        String string = response.toString();
        System.out.println(">>> " + string);
        socket.sendMore(string);
    }

    public void sendContinue(Object... data) {
        send(new Response(Response.CONTINUE, data));
    }

    public void sendOK(Object... data) {
        send(new Response(Response.OK, data));
    }

    public void sendCreated(Object... data) {
        send(new Response(Response.CREATED, data));
    }

    public void sendNoContent() {
        send(new Response(Response.NO_CONTENT));
    }

}
