package com.nigelsmall.borneo.resources;

import com.nigelsmall.borneo.Environment;
import com.nigelsmall.borneo.Request;
import com.nigelsmall.borneo.Response;
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

    final public static String PATTERN = "cypher";

    public CypherResource(Environment env, ZMQ.Socket socket) {
        super(env, socket);
    }

    /**
     * POST cypher <db_name> <query> [<params>]
     *
     * @param request
     */
    @Override
    public void post(Request request) {

        Object[] data = request.getData();

        if (data.length < 2) {
            send(Response.BAD_REQUEST, "Not enough terms");
            return;
        }

        String databaseName, query;
        try {
            databaseName = (String)data[0];
            query = (String)data[1];
        } catch (ClassCastException ex) {
            send(Response.BAD_REQUEST, "Database name and query must be stringular");
            return;
        }

        GraphDatabaseService database = environment().getDatabase(databaseName);
        ExecutionEngine engine = environment().getEngine(databaseName);

        try (Transaction tx = database.beginTx()) {

            ExecutionResult result;
            try {
                result = engine.execute( query );
            } catch (CypherException ex) {
                send(Response.BAD_REQUEST, ex.getMessage());
                return;
            }

            List<String> columns = result.columns();
            send(Response.CONTINUE, columns.toArray(new Object[columns.size()]));

            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                send(Response.CONTINUE, values.toArray(new Object[values.size()]));
            }
            tx.success();
        }

        send(Response.OK);

    }

}
