package org.zerograph.worker;

import org.zerograph.Service;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.except.ClientError;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseWorker<T extends Service> implements Runnable {

    final private UUID uuid;
    final private T service;
    final private ZMQ.Socket socket;

    public BaseWorker(T service) {
        this.uuid = UUID.randomUUID();
        this.service = service;
        this.socket = service.getContext().socket(ZMQ.REP);
        this.socket.connect(this.service.getInternalAddress());
    }

    public UUID getUUID() {
        return uuid;
    }

    public T getService() {
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
                    requests.add(new Request(line));
                }
            }
            more = socket.hasReceiveMore();
        }
        return requests;
    }

    public boolean send(Response response) {
        String string = response.toString();
        System.out.println(">>> " + string);
        return socket.send(string);
    }

}
