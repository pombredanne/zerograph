package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;
import org.zeromq.ZMQ;

import java.util.Map;

/**
 * Base class for all resources used by a Graph.
 *
 */
public abstract class AbstractTransactionalResource extends AbstractResource {

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;

    public AbstractTransactionalResource(ZerographInterface zerograph, ZMQ.Socket socket, GraphDatabaseService database) {
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

    public PropertyContainer get(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer put(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer patch(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer post(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer delete(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

}
