package com.nigelsmall.zerograph.resources;

import com.nigelsmall.zerograph.Request;
import com.nigelsmall.zerograph.Response;
import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.EntityNotFoundException;
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

    public CypherResource(GraphDatabaseService database, Transaction transaction, ZMQ.Socket socket) {
        super(database, transaction, socket);
    }

    /**
     * POST cypher {query} [{params}]
     *
     * @param request
     */
    @Override
    public void post(Request request) throws ClientError, ServerError {
        String query = getArgument(request, 0, String.class);
        try {
            ExecutionResult result = execute(query);
            List<String> columns = result.columns();
            sendContinue(columns.toArray(new Object[columns.size()]));
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                sendContinue(values.toArray(new Object[values.size()]));
            }
            sendOK();
        } catch (EntityNotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, ex.getMessage()));
        } catch (CypherException ex) {
            //ex.printStackTrace(System.err);
            throw new ClientError(new Response(Response.BAD_REQUEST, ex.getMessage()));
        }
    }

}
