package org.zerograph;

import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.api.ResourceInterface;
import org.zerograph.resource.GraphResource;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.ServerError;

import java.util.List;

public class ZerographWorker extends Worker<Zerograph> {

    final private GraphResource graphResource;
    final private Responder responder;

    public ZerographWorker(Zerograph zerograph) {
        super(zerograph, zerograph);
        this.responder = new Responder(this.getSocket());
        this.graphResource = new GraphResource(zerograph, this.responder);
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
                    String requestedResource = request.getResource();
                    if (graphResource.getName().equals(requestedResource)) {
                        resource = graphResource;
                    } else {
                        throw new NotFound("This service does not provide a resource called " + request.getResource());
                    }
                    switch (request.getMethod()) {
                        case "GET":
                            resource.get(request);
                            break;
                        case "PUT":
                            resource.put(request);
                            break;
                        case "PATCH":
                            resource.patch(request);
                            break;
                        case "POST":
                            resource.post(request);
                            break;
                        case "DELETE":
                            resource.delete(request);
                            break;
                        default:
                            throw new MethodNotAllowed(request.getMethod() + " " + request.getResource());
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
