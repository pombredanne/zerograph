package org.zerograph.zpp.api;

import org.zerograph.zpp.except.MalformedResponse;

import java.util.Map;

public interface ResponderInterface {

    public void beginResponseBatch();

    public void beginResponse() throws MalformedResponse;

    public void sendHead(Map<String, Object> data) throws MalformedResponse;

    public void sendBody(Object data) throws MalformedResponse;

    public void startBodyList() throws MalformedResponse;

    public void sendBodyItem(Object data) throws MalformedResponse;

    public void endBodyList() throws MalformedResponse;

    public void sendFoot(Map<String, Object> data) throws MalformedResponse;

    public void sendError(Exception ex);

    public void endResponse() throws MalformedResponse;

    public void endResponseBatch();

    public void close();

}
