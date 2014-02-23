package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import org.neo4j.graphdb.GraphDatabaseService;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        return env.getDatabase(getStringArgument(request, index));
    }

    public String getStringArgument(Request request, int index) throws BadRequest {
        try {
            return request.getStringData(index);
        } catch (IOException ex) {
            throw new BadRequest("String argument cannot be parsed");
        }
    }

    public int getIntegerArgument(Request request, int index) throws BadRequest {
        try {
            return request.getIntegerData(index);
        } catch (IOException ex) {
            throw new BadRequest("Integer argument cannot be parsed");
        }
    }

    public List getListArgument(Request request, int index) throws BadRequest {
        try {
            return request.getListData(index);
        } catch (IOException ex) {
            throw new BadRequest("List argument cannot be parsed");
        }
    }

    public Map getMapArgument(Request request, int index) throws BadRequest {
        try {
            return request.getMapData(index);
        } catch (IOException ex) {
            throw new BadRequest("Map argument cannot be parsed");
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
            case "POST":
                post(request);
                break;
            case "DELETE":
                delete(request);
                break;
            default:
                send(Response.METHOD_NOT_ALLOWED, request.getMethod());
        }
    }

    public void get(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getMethod());
    }

    public void put(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getMethod());
    }

    public void post(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getMethod());
    }

    public void delete(Request request) {
        send(Response.METHOD_NOT_ALLOWED, request.getMethod());
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

    public void send(Response response) {
        response.send(socket);
    }

}
