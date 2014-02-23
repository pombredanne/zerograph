package com.nigelsmall.borneo.resources;

import com.nigelsmall.borneo.BadRequest;
import com.nigelsmall.borneo.Environment;
import com.nigelsmall.borneo.Request;
import com.nigelsmall.borneo.Response;
import org.zeromq.ZMQ;

public abstract class Resource {

    final public static String PATTERN = null;

    final private Environment env;
    final private ZMQ.Socket socket;

    public Resource(Environment env, ZMQ.Socket socket) {
        this.env = env;
        this.socket = socket;
    }

    public Environment environment() {
        return this.env;
    }

    public Object getArgument(Request request, int index) throws BadRequest {
        try {
            return request.getData()[index];
        } catch (IndexOutOfBoundsException ex) {
            throw new BadRequest("Missing argument " + Integer.toString(index));
        }
    }

    public String getStringArgument(Request request, int index) throws BadRequest {
        try {
            return (String)getArgument(request, index);
        } catch (ClassCastException ex) {
            throw new BadRequest("String argument cannot be parsed");
        }
    }

    public long getLongArgument(Request request, int index) throws BadRequest {
        try {
            return ((Integer)getArgument(request, index)).longValue();
        } catch (ClassCastException ex) {
            throw new BadRequest("Long argument cannot be parsed");
        }
    }

    public void handle(Request request) {
        switch (request.getVerb()) {
            case "GET":
                get(request);
                break;
            case "PUT":
                put(request);
                break;
            case "POST":
                post(request);
                break;
            case "DELETE":
                delete(request);
                break;
            default:
                send(Response.METHOD_NOT_ALLOWED, request.getVerb());
        }
    }

    public void get(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getVerb());
    }

    public void put(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getVerb());
    }

    public void post(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getVerb());
    }

    public void delete(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getVerb());
    }

    public void send(int status) {
        send(status, new Object[0]);
    }

    public void send(int status, String message) {
        send(status, new String[]{message});
    }

    public void send(int status, Object[] data) {
        new Response(status, data).send(socket);
    }

}
