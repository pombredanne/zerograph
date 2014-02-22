package com.nigelsmall.borneo;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Worker implements Runnable {

    final public static String ADDRESS = "inproc://workers";

    private Environment env;
    private ZMQ.Socket external;
    private String databaseName;

    public Worker(Environment env) {
        this.env = env;
        this.external = env.getContext().socket(ZMQ.REP);
        this.external.connect(ADDRESS);
        this.databaseName = "default";
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String string = new String(external.recv(0), ZMQ.CHARSET);
            System.out.println("<<< " + string);
            try {
                Request request = new Request(string);
                handle(request);
            } catch (BadRequest badRequest) {
                badRequest.getResponse().send(external);
            } catch (IOException ex) {
                new Response(Response.SERVER_ERROR).send(external);
            }
            System.out.println();
        }
    }

    public void handle(Request request) throws IOException {
        if (request.getResource().equals("cypher")) {
            handleCypher(request);
        } else {
            new Response(Response.NOT_FOUND, new Object[] {request.getResource()}).send(external);
        }
    }

    // cypher Resource
    public void handleCypher(Request request) throws IOException {
        GraphDatabaseService database = env.getDatabase(databaseName);
        ExecutionEngine engine = env.getEngine(databaseName);

        if ( request.getVerb().equals("POST") ) {
            // POST cypher <query> [<params>]

            String query = (String)request.getData()[0];

            try ( Transaction tx = database.beginTx() )
            {
                ExecutionResult result = engine.execute( query );

                List<String> columns = result.columns();
                new Response(Response.CONTINUE, columns.toArray(new Object[columns.size()])).send(external);

                for (Map<String, Object> row : result) {
                    ArrayList<Object> values = new ArrayList<>();
                    for (String column : columns) {
                        values.add(row.get(column));
                    }
                    new Response(Response.CONTINUE, values.toArray(new Object[values.size()])).send(external);
                }
                tx.success();
            }

            new Response(Response.OK).send(external);

        } else {

            new Response(Response.METHOD_NOT_ALLOWED, new Object[] {request.getVerb()}).send(external);
        }

    }

}
