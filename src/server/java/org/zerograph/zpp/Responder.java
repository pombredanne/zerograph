package org.zerograph.zpp;

import org.zerograph.yaml.YAML;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.MalformedResponse;
import org.zeromq.ZMQ;

import java.util.Map;

public class Responder implements ResponderInterface {

    final private ZMQ.Socket socket;

    private boolean sentHead = false;
    private boolean sentBody = false;
    private boolean sentFoot = false;
    private boolean sentError = false;

    public Responder(ZMQ.Socket socket) {
        this.socket = socket;
    }

    public ZMQ.Socket getSocket() {
        return this.socket;
    }

    @Override
    public void beginResponse() throws MalformedResponse {
        sendMore("---");
        sentHead = false;
        sentBody = false;
        sentFoot = false;
        sentError = false;
    }

    @Override
    public void sendHead(Map<String, Object> data) throws MalformedResponse {
        if (!sentHead && !sentBody && !sentFoot && !sentError) {
            sendMore("head: " + YAML.dump(data));
            sentHead = true;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendBodyPart(Object data) throws MalformedResponse {
        if (!sentFoot && !sentError) {
            if (!sentBody) {
                sendMore("body:");
                sentBody = true;
            }
            sendMore("  - " + YAML.dump(data));
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendFoot(Map<String, Object> data) throws MalformedResponse {
        if (!sentFoot && !sentError) {
            sendMore("foot: " + YAML.dump(data));
            sentFoot = true;
        } else {
            throw new MalformedResponse();
        }
    }

    public void sendError(Exception ex) {
        if (!sentError) {
            sendMore("error: " + ex.getMessage());
            sentFoot = true;
        } else {
            sendMore("error: \"Malformed response\"");
        }
    }

    private void sendMore(String data) {
        System.out.println(">>> " + data);
        socket.sendMore(data + "\n");
    }

    public void endResponse() {
        //
    }

    @Override
    public void finish() {
        socket.send("");
    }
}
