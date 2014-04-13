package org.zerograph.zapp.api;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.zerograph.zapp.except.MalformedResponse;

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

    public Node sendNodes(Iterable<Node> result) throws MalformedResponse;

    public Relationship sendRelationships(Iterable<Relationship> result) throws MalformedResponse;

}
