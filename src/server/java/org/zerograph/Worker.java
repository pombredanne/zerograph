package org.zerograph;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.Neo4jContextInterface;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResourceInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;
import org.zerograph.api.ServiceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status4xx.MethodNotAllowed;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;
import org.zerograph.util.Data;
import org.zeromq.ZMQ;

import java.io.IOException;
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

    public List<Request> receiveRequestBatch() throws Status4xx {
        ArrayList<Request> requests = new ArrayList<>();
        boolean more = true;
        while (more) {
            String frame = socket.recvStr();
            for (String line : frame.split("\\r|\\n|\\r\\n")) {
                if (line.length() > 0) {
                    System.out.println("<<< " + line);
                    requests.add(new Request(line));
                }
            }
            more = socket.hasReceiveMore();
        }
        return requests;
    }

    public boolean send(ResponseInterface response) {
        StringBuilder builder = new StringBuilder(Integer.toString(response.getStatus()));
        for (Object datum : response.getData()) {
            builder.append('\t');
            try {
                builder.append(Data.encode(datum));
            } catch (IOException ex) {
                builder.append('?');  // TODO
            }
        }
        String string = builder.toString();
        System.out.println(">>> " + string);
        return socket.send(string);
    }

    protected PropertyContainer handle(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx {
        String requestedResourceName = request.getResourceName();
        if (resourceSet.contains(requestedResourceName)) {
            ResourceInterface resource = resourceSet.get(requestedResourceName);
            switch (request.getMethod()) {
                case "GET":
                    return resource.get(request, context);
                case "PUT":
                    return resource.put(request, context);
                case "PATCH":
                    return resource.patch(request, context);
                case "POST":
                    return resource.post(request, context);
                case "DELETE":
                    return resource.delete(request, context);
                default:
                    throw new MethodNotAllowed(request.getMethod() + " " + request.getResourceName());
            }
        } else {
            throw new NotFound("This service does not provide a resource called " + requestedResourceName);
        }
    }

}
