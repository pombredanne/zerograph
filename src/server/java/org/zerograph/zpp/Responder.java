package org.zerograph.zpp;

import org.zerograph.yaml.YAML;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.MalformedResponse;
import org.zeromq.ZMQ;

import java.util.Map;

public class Responder implements ResponderInterface {

    final private ZMQ.Socket socket;

    private int responseCount = 0;
    private boolean sentHead = false;
    private boolean startedBody = false;
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
    public void beginResponseBatch() {
        responseCount = 0;
    }

    @Override
    public void beginResponse() throws MalformedResponse {
        if (responseCount > 0) {
            sendMore("---");
        }
        sentHead = false;
        startedBody = false;
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
    public void sendBody(Object data) throws MalformedResponse {
        if (!startedBody && !sentBody && !sentFoot && !sentError) {
            sendMore("body: " + YAML.dump(data));
            startedBody = true;
            sentBody = true;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendBodyPart(Object data) throws MalformedResponse {
        if (!sentBody && !sentFoot && !sentError) {
            if (!startedBody) {
                sendMore("body:");
                startedBody = true;
            }
            sendMore("  - " + YAML.dump(data));
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendFoot(Map<String, Object> data) throws MalformedResponse {
        if (!sentFoot && !sentError) {
            sentBody = true;
            sendMore("foot: " + YAML.dump(data));
            sentFoot = true;
        } else {
            throw new MalformedResponse();
        }
    }

    public void sendError(Exception ex) {
        if (!sentError) {
            sendMore("error: " + ex.getMessage());
            sentError = true;
        } else {
            sendMore("error: \"Malformed response\"");
        }
    }

    private void sendMore(String data) {
        System.out.println(">>> " + data);
        socket.sendMore(data + "\n");
    }

    public void endResponse() {
        responseCount += 1;
    }

    @Override
    public void endResponseBatch() {
        socket.send("");
    }
}
