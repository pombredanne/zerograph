package org.zerograph.worker;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.Zerograph;
import org.zerograph.except.ClientError;
import org.zerograph.except.NotFound;
import org.zerograph.resource.GraphResource;

import java.util.ArrayList;
import java.util.List;

public class ZerographWorker extends BaseWorker<Zerograph> {

    final private GraphResource graphResource;

    public ZerographWorker(Zerograph zerograph) {
        super(zerograph, zerograph);
        this.graphResource = new GraphResource(zerograph, this.getSocket());
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
                System.out.println("--- Beginning batch in control worker " + this.getUUID().toString() + " ---");
                for (Request request : requests) {
                    request.resolvePointers(outputValues);
                    switch (request.getResource()) {
                        case GraphResource.NAME:
                            outputValues.add(graphResource.handle(request));
                            break;
                        default:
                            throw new NotFound("This service does not provide a resource called " + request.getResource());
                    }
                }
                send(new Response(Response.OK));
                System.out.println("--- Successfully completed batch in control worker " + this.getUUID().toString() + " ---");
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
