package org.zerograph.test.helpers;

import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;
import org.zerograph.zpp.except.MalformedResponse;

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
    public void sendBodyPart(Object data) throws MalformedResponse {
        body.add(data);
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

}
