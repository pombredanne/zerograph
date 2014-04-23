package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zerograph.util.Log;
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
                            Log.write(line, Log.RECEIVE);
                            requests.add(Request.parse(line));
                        }
                    }
                    more = socket.hasReceiveMore();
                }
                // action requests
                ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
                Log.write("Beginning transaction");
                try (Transaction tx = database.beginTx()) {
                    Database context = new Database(database, tx);  // TODO: construct higher up and just set tx here
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        outputValues.add(handle(request, context));
                    }
                    tx.success();
                }
                Log.write("Successfully completed transaction");
            } catch (ZMQException ex) {
                int errorCode = ex.getErrorCode();
                if (errorCode == 156384765) {
                    // shutting down
                    break;
                } else {
                    ex.printStackTrace(System.err);
                    throw ex;
                }
            } catch (IllegalArgumentException | ClientError | ServerError ex) {
                responder.sendError(ex);
            }
            responder.endResponseBatch();
        }
        responder.close();
    }

}
