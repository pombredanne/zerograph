package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status5xx.Abstract5xx;
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

    public PropertyContainer handle(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
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

    public PropertyContainer get(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer put(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer patch(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer post(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

    public PropertyContainer delete(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        throw new MethodNotAllowed(request.getMethod());
    }

}
