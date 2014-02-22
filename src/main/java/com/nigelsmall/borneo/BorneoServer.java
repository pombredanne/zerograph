package com.nigelsmall.borneo;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Experimental Neo4j Server using ZeroMQ
 *
 */
public class BorneoServer {

    final private static String STORAGE_DIR = "/tmp/borneo";

    private ZMQ.Context context = ZMQ.context(1);

    //  Socket to talk to clients
    private ZMQ.Socket socket = context.socket(ZMQ.REP);

    private String databaseHome;
    private GraphDatabaseService database;
    private ExecutionEngine engine;

    public BorneoServer(String address, String databaseHome) {
        this.socket.bind(address);
        this.databaseHome = databaseHome;
        this.database = openDatabase("default"); // just one db for now
        this.engine = new ExecutionEngine(database);
    }

    public void run() throws InterruptedException, IOException {
        while (!Thread.currentThread ().isInterrupted ()) {

            String string = new String(socket.recv(0), ZMQ.CHARSET);
            System.out.println("<<< " + string);

            try {
                Request request = new Request(string);
                handle(request);
            } catch (BadRequest badRequest) {
                badRequest.getResponse().send(socket);
            }

        }

        socket.close();
        context.term();
    }

    public void handle(Request request) throws IOException {

        if (request.getResource().equals("cypher")) {

            handleCypher(request);

        } else {

            new Response(Response.NOT_FOUND, new Object[] {request.getResource()}).send(socket);

        }

    }

    // cypher Resource
    public void handleCypher(Request request) throws IOException {

        if ( request.getVerb().equals("POST") ) {
            // POST cypher <query> [<params>]

            String query = (String)request.getData()[0];

            try ( Transaction tx = database.beginTx() )
            {
                ExecutionResult result = engine.execute( query );

                List<String> columns = result.columns();
                new Response(Response.CONTINUE, columns.toArray(new Object[columns.size()])).send(socket);

                for (Map<String, Object> row : result) {
                    ArrayList<Object> values = new ArrayList<>();
                    for (String column : columns) {
                        values.add(row.get(column));
                    }
                    new Response(Response.CONTINUE, values.toArray(new Object[values.size()])).send(socket);
                }
                tx.success();
            }

            new Response(Response.OK).send(socket);

        } else {

            new Response(Response.METHOD_NOT_ALLOWED, new Object[] {request.getVerb()}).send(socket);
        }

    }

    public GraphDatabaseService openDatabase(String name) {
        return new GraphDatabaseFactory().newEmbeddedDatabase( databaseHome + "/" + name );
    }

    public static void main (String[] args) throws Exception {

        BorneoServer server = new BorneoServer("tcp://*:47474", STORAGE_DIR);
        server.run();

    }
}
