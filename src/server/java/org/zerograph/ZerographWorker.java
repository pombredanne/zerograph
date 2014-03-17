package org.zerograph;

import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.ServerError;

import java.util.List;

public class ZerographWorker extends Worker<Zerograph> {

    public ZerographWorker(Zerograph zerograph) {
        super(zerograph, zerograph);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<Request> requests = receiveRequestBatch();
                System.out.println("--- Beginning batch in control worker " + this.getUUID().toString() + " ---");
                for (Request request : requests) {
                    handle(request, null);
                }
                send(new OK());
                System.out.println("--- Successfully completed batch in control worker " + this.getUUID().toString() + " ---");
            } catch (IllegalArgumentException ex) {
                send(new BadRequest(ex.getMessage()));
            } catch (TransactionFailureException ex) {
                send(new Conflict(ex.getMessage()));  // TODO - derive cause from nested Exceptions
            } catch (Status4xx ex) {
                send(ex);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                send(new ServerError(ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

}
