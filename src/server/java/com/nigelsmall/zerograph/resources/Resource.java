package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Map;

public abstract class Resource {

    final public static String NAME = null;

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;
    final private Transaction transaction;
    final private ZMQ.Socket socket;

    public Resource(GraphDatabaseService database, Transaction transaction, ZMQ.Socket socket) {
        this.database = database;
        this.transaction = transaction;
        this.engine = new ExecutionEngine(database);
        this.socket = socket;
    }

    public GraphDatabaseService database() {
        return this.database;
    }

    public ExecutionResult execute(String query) throws CypherException {
        return this.engine.execute(query);
    }

    public ExecutionResult execute(String query, Map<String, Object> params) throws CypherException {
        return this.engine.execute(query, params);
    }

    public ExecutionResult profile(String query, Map<String, Object> params) throws CypherException {
        return this.engine.profile(query, params);
    }

    public Lock acquireReadLock(PropertyContainer entity) {
        return transaction.acquireReadLock(entity);
    }

    public Lock acquireWriteLock(PropertyContainer entity) {
        return transaction.acquireWriteLock(entity);
    }

    public <T> T getArgument(Request request, int index, Class<T> klass) throws ClientError {
        try {
            return request.getData(index, klass);
        } catch (IOException ex) {
            throw new ClientError(new Response(Response.BAD_REQUEST, "Failed to parse argument " + index));
        }
    }

    public void handle(Request request) throws ClientError, ServerError {
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

    public void get(Request request) throws ClientError, ServerError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void put(Request request) throws ClientError, ServerError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void patch(Request request) throws ClientError, ServerError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void post(Request request) throws ClientError, ServerError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    public void delete(Request request) throws ClientError, ServerError {
        send(new Response(Response.METHOD_NOT_ALLOWED, request.getMethod()));
    }

    private void send(Response response) {
        response.send(socket, ZMQ.SNDMORE);
    }

    public void sendContinue(Object... data) {
        send(new Response(Response.CONTINUE, data));
    }

    public void sendOK(Object... data) {
        send(new Response(Response.OK, data));
    }

}
