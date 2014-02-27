package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.resources.CypherResource;
import com.nigelsmall.zerograph.resources.NodeResource;
import com.nigelsmall.zerograph.resources.NodeSetResource;
import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zeromq.ZMQ;

import java.util.UUID;

public class Worker implements Runnable {

    final public static String ADDRESS = "inproc://workers";

    final private UUID uuid;
    final private Environment env;
    final private GraphDatabaseService database;
    final private ZMQ.Socket external;

    public Worker(Environment env, int port) {
        this.uuid = UUID.randomUUID();
        this.env = env;
        this.database = env.getDatabase(port);
        this.external = env.getContext().socket(ZMQ.REP);
        this.external.connect(ADDRESS);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            boolean more = true;
            try (Transaction tx = database.beginTx()) {
                while (more) {
                    String frame = new String(external.recv(), ZMQ.CHARSET);
                    System.out.println("--- Received request in worker " + this.uuid.toString() + " ---");
                    for (String line : frame.split("\\r|\\n|\\r\\n")) {
                        System.out.println("<<< " + frame);
                        Request request = new Request(line);
                        switch (request.getResource()) {
                            case CypherResource.NAME:
                                new CypherResource(database, external).handle(request);
                                break;
                            case NodeResource.NAME:
                                new NodeResource(database, external).handle(request);
                                break;
                            case NodeSetResource.NAME:
                                new NodeSetResource(database, external).handle(request);
                                break;
                            default:
                                throw new ClientError(new Response(Response.NOT_FOUND, request.getResource()));
                        }
                    }
                    more = external.hasReceiveMore();
                }
                System.out.println("--- Completed transaction in worker " + this.uuid.toString() + " ---");
            } catch (TransactionFailureException ex) {
                new Response(Response.CONFLICT, ex.getMessage()).send(external);  // TODO - derive cause from nested Exceptions
            } catch (ClientError ex) {
                // 4XX
                ex.getResponse().send(external);
            } catch (RuntimeException ex) {
                // 5XX
                new Response(Response.SERVER_ERROR).send(external);
                // TODO: log
            } finally {
                System.out.println();
            }
        }
    }

}
