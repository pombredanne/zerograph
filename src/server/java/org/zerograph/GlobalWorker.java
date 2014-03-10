package org.zerograph;

import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.api.GlobalResourceInterface;
import org.zerograph.resource.GraphResource;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.Conflict;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.ServerError;

import java.util.List;

public class GlobalWorker extends Worker<Zerograph> {

    final private GraphResource graphResource;

    public GlobalWorker(Zerograph zerograph) {
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
            } catch (Abstract4xx ex) {
                send(ex);
                continue;
            }
            // handle requests
            try {
                System.out.println("--- Beginning batch in control worker " + this.getUUID().toString() + " ---");
                for (Request request : requests) {
                    GlobalResourceInterface resource;
                    switch (request.getResource()) {
                        case GraphResource.NAME:
                            resource = this.graphResource;
                            break;
                        default:
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
