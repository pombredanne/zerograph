package org.zerograph.zapp;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.zerograph.yaml.YAML;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.MalformedResponse;
import org.zeromq.ZMQ;

import java.util.Map;

public class Responder implements ResponderInterface {

    public static int ERROR = -1;
    public static int BEGIN = 0;
    public static int HEAD = 1;
    public static int BODY = 2;
    public static int START_BODY_LIST = 3;
    public static int BODY_ITEM = 4;
    public static int END_BODY_LIST = 5;
    public static int FOOT = 6;
    public static int END = 7;

    final private ZMQ.Socket socket;

    private int responseCount = 0;
    private int state;

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
        state = BEGIN;
    }

    @Override
    public void sendHead(Map<String, Object> data) throws MalformedResponse {
        if (state == BEGIN) {
            sendMore("head: " + YAML.dump(data));
            state = HEAD;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendBody(Object data) throws MalformedResponse {
        if (state == BEGIN || state == HEAD) {
            sendMore("body: " + YAML.dump(data));
            state = BODY;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void startBodyList() throws MalformedResponse {
        if (state == BEGIN || state == HEAD) {
            state = START_BODY_LIST;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendBodyItem(Object data) throws MalformedResponse {
        if (state == START_BODY_LIST) {
            sendMore("body:");
            sendMore("- " + YAML.dump(data));
            state = BODY_ITEM;
        } else if (state == BODY_ITEM) {
            sendMore("- " + YAML.dump(data));
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void endBodyList() throws MalformedResponse {
        if (state == START_BODY_LIST) {
            sendMore("body: []");
            state = END_BODY_LIST;
        } else if (state == BODY_ITEM) {
            state = END_BODY_LIST;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendFoot(Map<String, Object> data) throws MalformedResponse {
        if (state == BODY || state == END_BODY_LIST) {
            sendMore("foot: " + YAML.dump(data));
            state = FOOT;
        } else {
            throw new MalformedResponse();
        }
    }

    @Override
    public void sendError(Exception ex) {
        sendMore("error: " + YAML.dump(ex.getMessage()));
        state = ERROR;
    }

    @Override
    public void endResponse() {
        state = END;
        responseCount += 1;
    }

    @Override
    public void endResponseBatch() {
        socket.send("");
    }

    @Override
    public void close() {
        socket.close();
    }

    private void sendMore(String data) {
        System.out.println(">>> " + data);
        socket.sendMore(data + "\n");
    }

    @Override
    public Node sendNodes(Iterable<Node> result) throws MalformedResponse {
        Node first = null;
        startBodyList();
        for (Node rel : result) {
            if (first == null) {
                first = rel;
            }
            sendBodyItem(rel);
        }
        endBodyList();
        return first;
    }

    @Override
    public Relationship sendRelationships(Iterable<Relationship> result) throws MalformedResponse {
        Relationship first = null;
        startBodyList();
        for (Relationship rel : result) {
            if (first == null) {
                first = rel;
            }
            sendBodyItem(rel);
        }
        endBodyList();
        return first;
    }

}
