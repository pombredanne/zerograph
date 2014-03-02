package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.except.ClientError;
import com.nigelsmall.zerograph.except.ServerError;
import com.nigelsmall.zerograph.resources.CypherResource;
import com.nigelsmall.zerograph.resources.NodeResource;
import com.nigelsmall.zerograph.resources.NodeSetResource;
import com.nigelsmall.zerograph.resources.RelResource;
import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Worker implements Runnable {

    final public static String ADDRESS = "inproc://workers";

    final private UUID uuid;
    final private Environment env;
    final private GraphDatabaseService database;
    final private ZMQ.Socket external;

    final private CypherResource cypherResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;

    public Worker(Environment env, int port) {
        this.uuid = UUID.randomUUID();
        this.env = env;
        this.database = env.getDatabase(port);
        this.external = env.getContext().socket(ZMQ.REP);
        this.external.connect(ADDRESS);

        this.cypherResource = new CypherResource(this.database, this.external);
        this.nodeResource = new NodeResource(this.database, this.external);
        this.nodeSetResource = new NodeSetResource(this.database, this.external);
        this.relResource = new RelResource(this.database, this.external);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ArrayList<Request> requests = new ArrayList<>();
            // parse requests
            try {
                boolean more = true;
                while (more) {
                    String frame = external.recvStr();
                    for (String line : frame.split("\\r|\\n|\\r\\n")) {
                        if (line.length() > 0) {
                            System.out.println("<<< " + line);
                            requests.add(new Request(line));
                        }
                    }
                    more = external.hasReceiveMore();
                }
            } catch (ClientError ex) {
                ex.getResponse().send(external, 0);
                continue;
            }
            // handle requests
            ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
            try (Transaction tx = database.beginTx()) {
                System.out.println("--- Began transaction in worker " + this.uuid.toString() + " ---");
                for (Request request : requests) {
                    request.resolvePointers(outputValues);
                    switch (request.getResource()) {
                        case CypherResource.NAME:
                            outputValues.add(cypherResource.handle(tx, request));
                            break;
                        case NodeResource.NAME:
                            outputValues.add(nodeResource.handle(tx, request));
                            break;
                        case NodeSetResource.NAME:
                            outputValues.add(nodeSetResource.handle(tx, request));
                            break;
                        case RelResource.NAME:
                            outputValues.add(relResource.handle(tx, request));
                            break;
                        default:
                            throw new ClientError(new Response(Response.NOT_FOUND, request.getResource()));
                    }
                }
                external.send("");
                tx.success();
                System.out.println("--- Completed transaction in worker " + this.uuid.toString() + " ---");
            } catch (TransactionFailureException ex) {
                new Response(Response.CONFLICT, ex.getMessage()).send(external, 0);  // TODO - derive cause from nested Exceptions
            } catch (ClientError ex) {
                //ex.printStackTrace(System.err);
                // 4XX
                ex.getResponse().send(external, 0);
            } catch (ServerError ex) {
                //ex.printStackTrace(System.err);
                // 5XX
                new Response(Response.SERVER_ERROR).send(external, 0);
                // TODO: log
            } finally {
                System.out.println();
            }
        }
    }

}
