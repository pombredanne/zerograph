package org.zerograph.service;

import org.zerograph.zpp.Request;

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

                System.out.println("--- Successfully completed batch in control worker " + this.getUUID().toString() + " ---");
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                responder.sendError(ex);
            } finally {
                responder.finish();
                System.out.println();
            }
        }
    }

}
