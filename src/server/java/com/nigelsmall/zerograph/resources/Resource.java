package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.graphdb.GraphDatabaseService;
import org.zeromq.ZMQ;

import java.io.IOException;

public abstract class Resource {

    final public static String NAME = null;

    final private Environment env;
    final private ZMQ.Socket socket;

    public Resource(Environment env, ZMQ.Socket socket) {
        this.env = env;
        this.socket = socket;
    }

    public Environment environment() {
        return this.env;
    }

    public GraphDatabaseService getDatabaseArgument(Request request, int index) throws BadRequest {
        return env.getDatabase(getArgument(request, index, String.class));
    }

    public <T> T getArgument(Request request, int index, Class<T> klass) throws BadRequest {
        try {
            return request.getData(index, klass);
        } catch (IOException ex) {
            throw new BadRequest("Failed to parse argument " + index);
        }
    }

    public void handle(Request request) {
        switch (request.getMethod()) {
            case "GET":
                get(request);
                break;
            case "PUT":
                put(request);
                break;
            case "PATCH":
                patch(request);
                break;
            case "POST":
                post(request);
                break;
            case "DELETE":
                delete(request);
                break;
            default:
                send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
        }
    }

    public void get(Request request) {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void put(Request request) {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void patch(Request request) {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void post(Request request) {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void delete(Request request) {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void send(Response response) {
        response.send(socket);
    }

}
