package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.zpp.Request;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;
import org.zeromq.ZMQException;

import java.util.ArrayList;

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
        while (true) {
            try {
                // receive request batch
                ArrayList<Request> requests = new ArrayList<>();
                boolean more = true;
                while (more) {
                    String frame = socket.recvStr();
                    for (String line : frame.split("\\r|\\n|\\r\\n")) {
                        if (line.length() > 0) {
                            System.out.println("<<< " + line);
                            requests.add(Request.parse(line));
                        }
                    }
                    more = socket.hasReceiveMore();
                }
                // action requests
                ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
                System.out.println("--- Beginning transaction for graph " + service.getPort() + " in worker " + this.getUUID().toString() + " ---");
                try (Transaction tx = database.beginTx()) {
                    Database context = new Database(database, tx);  // TODO: construct higher up and just set tx here
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        outputValues.add(handle(request, context));
                    }
                    tx.success();
                }
                System.out.println("--- Successfully completed transaction in worker " + this.getUUID().toString() + " ---");
            } catch (ZMQException ex) {
                int errorCode = ex.getErrorCode();
                if (errorCode == 156384765) {
                    // shutting down
                    break;
                } else {
                    ex.printStackTrace(System.err);
                    throw ex;
                }
            } catch (ClientError | ServerError ex) {
                responder.sendError(ex);
            }
            responder.endResponseBatch();
            System.out.println();
        }
        responder.close();
    }

}
