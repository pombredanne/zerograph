package org.zerograph.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.zerograph.test.helpers.QuickMap;
import org.zerograph.resources.NodeResource;
import org.zerograph.Request;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;

import java.util.ArrayList;

public class NodeResourceTest extends ResourceTest {

    protected NodeResource resource;

    @Before
    public void createResource() {
        resource = new NodeResource(responseCollector);
    }

    @Test
    public void testCanGetExistingNode() throws ClientError, ServerError {
        Node created = createNode(ALICE);
        Request request = new Request("GET", "node", QuickMap.from("id", created.getId()));
        Node got = (Node)resource.get(request, context);
        assert ALICE.equals(got);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(created);
    }

    @Test(expected=ClientError.class)
    public void testCannotGetNonExistentNode() throws ClientError, ServerError {
        Request request = new Request("GET", "node", QuickMap.from("id", 0));
        resource.get(request, context);
    }

    @Test
    public void testCanSetExistingNode() throws ClientError, ServerError {
        Node created = createNode();
        Request request = new Request("PUT", "node",
                QuickMap.from("id", created.getId(),
                              "labels", new ArrayList<>(ALICE.getLabels()),
                              "properties", ALICE.getProperties()));
        Node put = (Node)resource.set(request, context);
        assert ALICE.equals(put);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(put);
    }

    @Test(expected=ClientError.class)
    public void testCannotSetNonExistentNode() throws ClientError, ServerError {
        Request request = new Request("PUT", "node",
                QuickMap.from("id", 0,
                              "labels", new ArrayList<>(ALICE.getLabels()),
                              "properties", ALICE.getProperties()));
        resource.set(request, context);
    }

    @Test
    public void testCanPatchExistingNode() throws ClientError, ServerError {
        Node created = createNode(ALICE);
        Request request = new Request("PATCH", "node",
                QuickMap.from("id", created.getId(),
                              "labels", new ArrayList<>(EMPLOYEE.getLabels()),
                              "properties", EMPLOYEE.getProperties()));
        Node patched = (Node)resource.patch(request, context);
        assert ALICE_THE_EMPLOYEE.equals(patched);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(patched);
    }

    @Test(expected=ClientError.class)
    public void testCannotPatchNonExistentNode() throws ClientError, ServerError {
        Request request = new Request("PATCH", "node",
                QuickMap.from("id", 0,
                              "labels", new ArrayList<>(EMPLOYEE.getLabels()),
                              "properties", EMPLOYEE.getProperties()));
        resource.set(request, context);
    }

    @Test
    public void testCanCreateNode() throws ClientError, ServerError {
        Request request = new Request("POST", "node", QuickMap.from("labels", new ArrayList<>(ALICE.getLabels()), "properties", ALICE.getProperties()));
        Node created = (Node)resource.create(request, context);
        assert ALICE.equals(created);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(created);
    }

    @Test
    public void testCanDeleteExistingNode() throws ClientError, ServerError {
        Node created = database.createNode();
        Request request = new Request("DELETE", "node", QuickMap.from("id", created.getId()));
        resource.delete(request, context);
        assert responseCollector.getBody().size() == 0;
    }

    @Test(expected=ClientError.class)
    public void testCannotDeleteNonExistentNode() throws ClientError, ServerError {
        Request request = new Request("DELETE", "node", QuickMap.from("id", 0));
        resource.delete(request, context);
    }

}
