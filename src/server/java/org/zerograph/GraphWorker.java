package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.ServerError;

import java.util.ArrayList;
import java.util.List;

public class GraphWorker extends Worker<Graph> {

    final private GraphDatabaseService database;

    public GraphWorker(ZerographInterface zerograph, Graph graph) {
        super(zerograph, graph);
        this.database = graph.getDatabase();
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
                    Neo4jContext context = new Neo4jContext(database, tx);  // TODO: construct higher up and just set tx here
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        ResourceInterface resource;
                        String requestedResourceName = request.getResourceName();
                        if (resourceSet.contains(requestedResourceName)) {
                            resource = resourceSet.get(requestedResourceName);
                        } else {
                            throw new NotFound("This service does not provide a resource called " + request.getResourceName());
                        }
                        PropertyContainer outputValue;
                        switch (request.getMethod()) {
                            case "GET":
                                outputValue = resource.get(context, request);
                                break;
                            case "PUT":
                                outputValue = resource.put(context, request);
                                break;
                            case "PATCH":
                                outputValue = resource.patch(context, request);
                                break;
                            case "POST":
                                outputValue = resource.post(context, request);
                                break;
                            case "DELETE":
                                outputValue = resource.delete(context, request);
                                break;
                            default:
                                throw new MethodNotAllowed(request.getMethod() + " " + request.getResourceName());
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
