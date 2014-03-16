package org.zerograph;

import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.api.ResourceInterface;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
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
            List<Request> requests;
            // parse requests
            try {
                requests = receiveRequestBatch();
            } catch (Status4xx ex) {
                send(ex);
                continue;
            }
            // handle requests
            try {
                System.out.println("--- Beginning batch in control worker " + this.getUUID().toString() + " ---");
                for (Request request : requests) {
                    ResourceInterface resource;
                    String requestedResourceName = request.getResourceName();
                    if (resourceSet.contains(requestedResourceName)) {
                        resource = resourceSet.get(requestedResourceName);
                    } else {
                        throw new NotFound("This service does not provide a resource called " + request.getResourceName());
                    }
                    switch (request.getMethod()) {
                        case "GET":
                            resource.get(null, request);
                            break;
                        case "PUT":
                            resource.put(null, request);
                            break;
                        case "PATCH":
                            resource.patch(null, request);
                            break;
                        case "POST":
                            resource.post(null, request);
                            break;
                        case "DELETE":
                            resource.delete(null, request);
                            break;
                        default:
                            throw new MethodNotAllowed(request.getMethod() + " " + request.getResourceName());
                    }
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
                send(new ServerError(ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

}
