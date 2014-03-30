package org.zerograph;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.ResourceSet;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.api.ServiceInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.Request;
import org.zerograph.zpp.Responder;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.MethodNotAllowed;
import org.zerograph.zpp.except.ServerError;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Worker<S extends ServiceInterface> implements Runnable {

    final private UUID uuid;
    final private S service;
    final private ZMQ.Socket socket;

    final protected ResponderInterface responder;
    final protected ResourceSet resourceSet;

    public Worker(S service) {
        this.uuid = UUID.randomUUID();
        this.service = service;
        this.socket = service.getContext().socket(ZMQ.REP);
        this.socket.connect(this.service.getInternalAddress());
        this.responder = new Responder(this.getSocket());
        this.resourceSet = service.createResourceSet(responder);
    }

    public UUID getUUID() {
        return uuid;
    }

    public S getService() {
        return service;
    }

    public ZMQ.Socket getSocket() {
        return this.socket;
    }

    public List<Request> receiveRequestBatch() throws ClientError {
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
        return requests;
    }

    protected PropertyContainer handle(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String requestedResource = request.getResource();
        if (resourceSet.contains(requestedResource)) {
            ResourceInterface resource = resourceSet.get(requestedResource);
            PropertyContainer entity;
            responder.beginResponse();
            switch (request.getMethod().charAt(0)) {
                case 'G':  // GET
                    entity = resource.get(request, database);
                    break;
                case 'S':  // SET
                    entity = resource.set(request, database);
                    break;
                case 'P':  // PATCH
                    entity = resource.patch(request, database);
                    break;
                case 'C':  // CREATE
                    entity = resource.create(request, database);
                    break;
                case 'D':  // DELETE
                    entity = resource.delete(request, database);
                    break;
                case 'E':  // EXECUTE
                case 'X':  // EXECUTE
                    entity = resource.execute(request, database);
                    break;
                default:
                    throw new MethodNotAllowed(request.getMethod() + " " + request.getResource());
            }
            responder.endResponse();
            return entity;
        } else {
            throw new ClientError("This service does not provide a resource called " + requestedResource);
        }
    }

}
