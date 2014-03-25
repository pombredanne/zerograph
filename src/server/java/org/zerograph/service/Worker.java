package org.zerograph.service;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.ResourceSet;
import org.zerograph.neo4j.api.DatabaseInterface;
import org.zerograph.service.api.ServiceInterface;
import org.zerograph.service.api.ZerographInterface;
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

    final private ZerographInterface zerograph;
    final private UUID uuid;
    final private S service;
    final private ZMQ.Socket socket;

    final protected ResponderInterface responder;
    final protected ResourceSet resourceSet;

    public Worker(ZerographInterface zerograph, S service) {
        this.zerograph = zerograph;
        this.uuid = UUID.randomUUID();
        this.service = service;
        this.socket = service.getContext().socket(ZMQ.REP);
        this.socket.connect(this.service.getInternalAddress());
        this.responder = new Responder(this.getSocket());
        this.resourceSet = service.createResourceSet(responder);
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
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
            switch (request.getMethod()) {
                case "GET":
                    entity = resource.get(request, database);
                    break;
                case "SET":
                    entity = resource.set(request, database);
                    break;
                case "PATCH":
                    entity = resource.patch(request, database);
                    break;
                case "CREATE":
                    entity = resource.create(request, database);
                    break;
                case "DELETE":
                    entity = resource.delete(request, database);
                    break;
                case "EXECUTE":
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
