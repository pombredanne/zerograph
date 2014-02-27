package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CypherResource extends Resource {

    final public static String NAME = "cypher";

    public CypherResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * POST cypher {query} [{params}]
     *
     * @param request
     */
    @Override
    public void post(Request request) throws ClientError {
        String query = getArgument(request, 0, String.class);
        try (Transaction tx = database().beginTx()) {
            ExecutionEngine engine = new ExecutionEngine(database());
            ExecutionResult result;
            result = engine.execute(query);
            List<String> columns = result.columns();
            send(new Response(Response.CONTINUE, columns.toArray(new Object[columns.size()])));
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                send(new Response(Response.CONTINUE, values.toArray(new Object[values.size()])));
            }
            tx.success();
            send(new Response(Response.OK));
        } catch (CypherException ex) {
            send(new Response(Response.BAD_REQUEST, ex.getMessage()));
        }
    }

}
