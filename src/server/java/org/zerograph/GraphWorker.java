package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.resource.CypherResource;
import org.zerograph.resource.NodeResource;
import org.zerograph.resource.NodeSetResource;
import org.zerograph.resource.RelResource;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.ServerError;

import java.util.ArrayList;
import java.util.List;

public class GraphWorker extends Worker<Graph> {

    final private GraphDatabaseService database;
    final private Responder responder;

    final private CypherResource cypherResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;

    public GraphWorker(ZerographInterface zerograph, Graph graph) {
        super(zerograph, graph);
        this.database = graph.getDatabase();
        this.responder = new Responder(this.getSocket());
        this.cypherResource = new CypherResource(zerograph, this.responder, this.database);
        this.nodeResource = new NodeResource(zerograph, this.responder, this.database);
        this.nodeSetResource = new NodeSetResource(zerograph, this.responder, this.database);
        this.relResource = new RelResource(zerograph, this.responder, this.database);
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
            } catch (Status4xx ex) {
                send(ex);
                continue;
            }
            // handle requests
            ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
            try {
                System.out.println("--- Beginning transaction in worker " + this.getUUID().toString() + " ---");
                try (Transaction tx = database.beginTx()) {
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        TransactionalResourceInterface resource;
                        String requestedResource = request.getResource();
                        if (cypherResource.getName().equals(requestedResource)) {
                            resource = cypherResource;
                        } else if (nodeResource.getName().equals(requestedResource)) {
                            resource = nodeResource;
                        } else if (relResource.getName().equals(requestedResource)) {
                            resource = relResource;
                        } else if (nodeSetResource.getName().equals(requestedResource)) {
                            resource = nodeSetResource;
                        } else {
                            throw new NotFound("This service does not provide a resource called " + request.getResource());
                        }
                        PropertyContainer outputValue;
                        switch (request.getMethod()) {
                            case "GET":
                                outputValue = resource.get(request, tx);
                                break;
                            case "PUT":
                                outputValue = resource.put(request, tx);
                                break;
                            case "PATCH":
                                outputValue = resource.patch(request, tx);
                                break;
                            case "POST":
                                outputValue = resource.post(request, tx);
                                break;
                            case "DELETE":
                                outputValue = resource.delete(request, tx);
                                break;
                            default:
                                throw new MethodNotAllowed(request.getMethod() + " " + request.getResource());
                        }
                        outputValues.add(outputValue);
                    }
                    tx.success();
                }
                send(new OK());
                System.out.println("--- Successfully completed transaction in worker " + this.getUUID().toString() + " ---");
            } catch (IllegalArgumentException ex) {
                send(new BadRequest(ex.getMessage()));
            } catch (TransactionFailureException ex) {
                send(new Conflict(ex.getMessage()));  // TODO - derive cause from nested Exceptions
            } catch (Status4xx ex) {
                send(ex);
            } catch (Exception ex) {
                send(new ServerError(ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

}
