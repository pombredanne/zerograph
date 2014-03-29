package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.zpp.Request;

import java.util.ArrayList;
import java.util.List;

public class GraphWorker extends Worker<Graph> {

    final private GraphDatabaseService database;

    public GraphWorker(Graph graph) {
        super(graph);
        this.database = graph.getDatabase();
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<Request> requests = receiveRequestBatch();
                ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
                System.out.println("--- Beginning transaction in worker " + this.getUUID().toString() + " ---");
                try (Transaction tx = database.beginTx()) {
                    Database context = new Database(database, tx);  // TODO: construct higher up and just set tx here
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        outputValues.add(handle(request, context));
                    }
                    tx.success();
                }
                System.out.println("--- Successfully completed transaction in worker " + this.getUUID().toString() + " ---");
            } catch (Exception ex) {
                responder.sendError(ex);
                ex.printStackTrace(System.err);
            } finally {
                responder.endResponseBatch();
                System.out.println();
            }
        }
    }

}
