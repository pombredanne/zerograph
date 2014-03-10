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
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.ServerError;

import java.util.ArrayList;
import java.util.List;

public class TransactionalWorker extends Worker<Graph> {

    final private GraphDatabaseService database;

    final private CypherResource cypherResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;

    public TransactionalWorker(ZerographInterface zerograph, Graph graph) {
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
            } catch (Abstract4xx ex) {
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
                        switch (request.getResource()) {
                            case CypherResource.NAME:
                                resource = this.cypherResource;
                                break;
                            case NodeResource.NAME:
                                resource = this.nodeResource;
                                break;
                            case NodeSetResource.NAME:
                                resource = this.nodeSetResource;
                                break;
                            case RelResource.NAME:
                                resource = this.relResource;
                                break;
                            default:
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
            } catch (Abstract4xx ex) {
                send(ex);
            } catch (Exception ex) {
                send(new ServerError(ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

}
