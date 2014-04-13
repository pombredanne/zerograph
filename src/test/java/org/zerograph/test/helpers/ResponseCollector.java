package org.zerograph.test.helpers;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.MalformedResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseCollector implements ResponderInterface {

    private HashMap<String, Object> head;
    private ArrayList<Object> body;
    private HashMap<String, Object> foot;

    public ResponseCollector() {
        head = new HashMap<>();
        body = new ArrayList<>();
        foot = new HashMap<>();
    }

    public Map<String, Object> getHead() {
        return head;
    }

    public List<Object> getBody() {
        return body;
    }

    public Map<String, Object> getFoot() {
        return foot;
    }

    @Override
    public void beginResponseBatch() {

    }

    @Override
    public void beginResponse() throws MalformedResponse {

    }

    @Override
    public void sendHead(Map<String, Object> data) throws MalformedResponse {
        head.putAll(data);
    }

    @Override
    public void sendBody(Object data) throws MalformedResponse {
        body.add(data);
    }

    @Override
    public void startBodyList() throws MalformedResponse {

    }

    @Override
    public void sendBodyItem(Object data) throws MalformedResponse {
        body.add(data);
    }

    @Override
    public void endBodyList() throws MalformedResponse {

    }

    @Override
    public void sendFoot(Map<String, Object> data) throws MalformedResponse {
        foot.putAll(data);
    }

    @Override
    public void sendError(Exception ex) {

    }

    @Override
    public void endResponse() throws MalformedResponse {

    }

    @Override
    public void endResponseBatch() {

    }

    @Override
    public void close() {

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
