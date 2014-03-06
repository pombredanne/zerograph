package org.zerograph.test.resources;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zerograph.resources.NodeResource;

public class NodeResourceTest extends ResourceTest {

    protected NodeResource resource;

    @Before
    public void createResource() {
        resource = new NodeResource(database, server);
    }

    @Test
    public void testCanCreateNode() throws ClientError, ServerError {
        Request request = new Request("POST\tnode\t[\"Person\"]\t{\"name\":\"Alice\"}");
        try (Transaction tx = database.beginTx()) {
            PropertyContainer entity = resource.post(tx, request);
            assert entity instanceof Node;
            Node node = (Node)entity;
            assert node.hasLabel(DynamicLabel.label("Person"));
            assert node.hasProperty("name");
            assert node.getProperty("name").equals("Alice");
        }
    }

}
