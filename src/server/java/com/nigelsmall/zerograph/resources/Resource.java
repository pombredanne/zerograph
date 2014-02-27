package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Map;

public abstract class Resource {

    final public static String NAME = null;

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;
    final private ZMQ.Socket socket;

    public Resource(GraphDatabaseService database, ZMQ.Socket socket) {
        this.database = database;
        this.engine = new ExecutionEngine(database);
        this.socket = socket;
    }

    public GraphDatabaseService database() {
        return this.database;
    }

    public ExecutionResult execute(String query, Map<String, Object> params) throws CypherException {
        return this.engine.execute(query, params);
    }

    public ExecutionResult profile(String query, Map<String, Object> params) throws CypherException {
        return this.engine.profile(query, params);
    }

    public <T> T getArgument(Request request, int index, Class<T> klass) throws ClientError {
        try {
            return request.getData(index, klass);
        } catch (IOException ex) {
            throw new ClientError(new Response(Response.BAD_REQUEST, "Failed to parse argument " + index));
        }
    }

    public void handle(Request request) throws ClientError {
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

    public void get(Request request) throws ClientError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void put(Request request) throws ClientError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void patch(Request request) throws ClientError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void post(Request request) throws ClientError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void delete(Request request) throws ClientError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void send(Response response) {
        response.send(socket);
    }

}
