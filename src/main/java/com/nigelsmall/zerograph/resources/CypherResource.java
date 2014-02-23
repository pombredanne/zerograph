package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.BadRequest;
import com.nigelsmall.zerograph.Environment;
import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
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

    public CypherResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * POST cypher {db} {query} [{params}]
     *
     * @param request
     */
    @Override
    public void post(Request request) {
        Response response = new Response(Response.SERVER_ERROR);
        try {
            GraphDatabaseService database = getDatabaseArgument(request, 0);
            String query = getStringArgument(request, 1);
            try (Transaction tx = database.beginTx()) {
                ExecutionEngine engine = new ExecutionEngine(database);
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
                response = new Response(Response.OK);
            }
        } catch (BadRequest ex) {
            response = ex.getResponse();
        } catch (CypherException ex) {
            response = new Response(Response.BAD_REQUEST, ex.getMessage());
        } finally {
            send(response);
        }
    }

}
