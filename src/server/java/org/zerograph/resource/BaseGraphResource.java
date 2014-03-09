package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.MethodNotAllowed;
import org.zerograph.except.ServerError;
import org.zeromq.ZMQ;

import java.util.Map;

/**
 * Base class for all resources used by a Graph.
 *
 */
public abstract class BaseGraphResource extends BaseResource {

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;

    public BaseGraphResource(Zerograph zerograph, ZMQ.Socket socket, GraphDatabaseService database) {
        super(zerograph, socket);
        this.database = database;
        this.engine = new ExecutionEngine(database);
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

    public PropertyContainer handle(Request request, Transaction tx) throws ClientError, ServerError {
        switch (request.getMethod()) {
            case "GET":
                return get(request, tx);
            case "PUT":
                return put(request, tx);
            case "PATCH":
                return patch(request, tx);
            case "POST":
                return post(request, tx);
            case "DELETE":
                return delete(request, tx);
            default:
                throw new MethodNotAllowed(request.getMethod());
        }
    }

    public PropertyContainer get(Request request, Transaction tx) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer put(Request request, Transaction tx) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer patch(Request request, Transaction tx) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer post(Request request, Transaction tx) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer delete(Request request, Transaction tx) throws ClientError, ServerError {
        throw new MethodNotAllowed(request.getMethod());
    }

}
