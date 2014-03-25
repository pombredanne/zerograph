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
        socket.sendMore("---\n");
        sentHead = false;
        sentBody = false;
        sentFoot = false;
        sentError = false;
    }

    @Override
    public void sendHead(Map<String, Object> data) throws MalformedResponse {
        if (!sentHead && !sentBody && !sentFoot && !sentError) {
            socket.sendMore("head: " + YAML.dump(data) + "\n");
            sentHead = true;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendBodyPart(Object data) throws MalformedResponse {
        if (!sentFoot && !sentError) {
            if (!sentBody) {
                socket.sendMore("body:\n");
                sentBody = true;
            }
            socket.sendMore("  - " + YAML.dump(data) + "\n");
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendFoot(Map<String, Object> data) throws MalformedResponse {
        if (!sentFoot && !sentError) {
            socket.sendMore("foot: " + YAML.dump(data) + "\n");
            sentFoot = true;
        } else {
            throw new MalformedResponse();
        }
    }

    public void sendError(Exception ex) {
        if (!sentError) {
            socket.sendMore("error: " + ex.getMessage() + "\n");
            sentFoot = true;
        } else {
            socket.sendMore("error: \"Malformed response\"\n");
        }
    }

    public void endResponse() {
        //
    }

    @Override
    public void finish() {
        socket.send("");
    }
}
