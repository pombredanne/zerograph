package org.zerograph;

import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;
import org.zerograph.util.Data;
import org.zeromq.ZMQ;

import java.io.IOException;

public class Responder implements ResponderInterface {

    final private ZMQ.Socket socket;

    public Responder(ZMQ.Socket socket) {
        this.socket = socket;
    }

    public ZMQ.Socket getSocket() {
        return this.socket;
    }

    @Override
    public void respond(ResponseInterface response) {
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
        socket.sendMore(string);
    }

}
