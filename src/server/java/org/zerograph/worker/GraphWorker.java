package org.zerograph.worker;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.Graph;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.NotFound;
import org.zerograph.resource.CypherResource;
import org.zerograph.resource.NodeResource;
import org.zerograph.resource.NodeSetResource;
import org.zerograph.resource.RelResource;

import java.util.ArrayList;
import java.util.List;

public class GraphWorker extends BaseWorker<Graph> {

    final private GraphDatabaseService database;

    final private CypherResource cypherResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;

    public GraphWorker(Zerograph zerograph, Graph graph) {
        super(zerograph, graph);
        this.database = graph.getDatabase();
        this.cypherResource = new CypherResource(zerograph, this.getSocket(), this.database);
        this.nodeResource = new NodeResource(zerograph, this.getSocket(), this.database);
        this.nodeSetResource = new NodeSetResource(zerograph, this.getSocket(), this.database);
        this.relResource = new RelResource(zerograph, this.getSocket(), this.database);
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Request> requests;
            // parse requests
            try {
                requests = receiveRequestBatch();
            } catch (ClientError ex) {
                send(ex.getResponse());
                continue;
            }
            // handle requests
            ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
            try {
                System.out.println("--- Beginning transaction in worker " + this.getUUID().toString() + " ---");
                try (Transaction tx = database.beginTx()) {
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        switch (request.getResource()) {
                            case CypherResource.NAME:
                                outputValues.add(cypherResource.handle(request, tx));
                                break;
                            case NodeResource.NAME:
                                outputValues.add(nodeResource.handle(request, tx));
                                break;
                            case NodeSetResource.NAME:
                                outputValues.add(nodeSetResource.handle(request, tx));
                                break;
                            case RelResource.NAME:
                                outputValues.add(relResource.handle(request, tx));
                                break;
                            default:
                                throw new NotFound("This service does not provide a resource called " + request.getResource());
                        }
                    }
                    tx.success();
                }
                send(new Response(Response.OK));
                System.out.println("--- Successfully completed transaction in worker " + this.getUUID().toString() + " ---");
            } catch (IllegalArgumentException ex) {
                send(new Response(Response.BAD_REQUEST, ex.getMessage()));
            } catch (TransactionFailureException ex) {
                send(new Response(Response.CONFLICT, ex.getMessage()));  // TODO - derive cause from nested Exceptions
            } catch (ClientError ex) {
                send(ex.getResponse());
            } catch (Exception ex) {
                send(new Response(Response.SERVER_ERROR, ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

}
