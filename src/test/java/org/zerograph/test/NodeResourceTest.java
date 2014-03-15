package org.zerograph.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.zerograph.resource.NodeResource;
import org.zerograph.response.status2xx.Status2xx;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;
import org.zerograph.test.helpers.FakeRequest;

import java.util.ArrayList;

public class NodeResourceTest extends ResourceTest {

    protected NodeResource resource;

    @Before
    public void createResource() {
        resource = new NodeResource(fakeZerograph, responseCollector, database);
    }

    @Test
    public void testCanGetExistingNode() throws Status4xx, Status5xx {
        Node created = createNode(ALICE);
        FakeRequest request = new FakeRequest("GET", "node", created.getId());
        Node got = (Node)resource.get(request, tx);
        assert ALICE.equals(got);
        assert responseCollector.matchSingleResponse(Status2xx.OK, created);
    }

    @Test(expected=NotFound.class)
    public void testCannotGetNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("GET", "node", 0);
        resource.get(request, tx);
    }

    @Test
    public void testCanPutExistingNode() throws Status4xx, Status5xx {
        Node created = createNode();
        FakeRequest request = new FakeRequest("PUT", "node", created.getId(), new ArrayList<>(ALICE.getLabels()), ALICE.getProperties());
        Node put = (Node)resource.put(request, tx);
        assert ALICE.equals(put);
        assert responseCollector.matchSingleResponse(Status2xx.OK, put);
    }

    @Test(expected=NotFound.class)
    public void testCannotPutNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PUT", "node", 0, new ArrayList<>(ALICE.getLabels()), ALICE.getProperties());
        resource.put(request, tx);
    }

    @Test
    public void testCanPatchExistingNode() throws Status4xx, Status5xx {
        Node created = createNode(ALICE);
        FakeRequest request = new FakeRequest("PATCH", "node", created.getId(), new ArrayList<>(EMPLOYEE.getLabels()), EMPLOYEE.getProperties());
        Node patched = (Node)resource.patch(request, tx);
        assert ALICE_THE_EMPLOYEE.equals(patched);
        assert responseCollector.matchSingleResponse(Status2xx.OK, patched);
    }

    @Test(expected=NotFound.class)
    public void testCannotPatchNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PATCH", "node", 0, new ArrayList<>(EMPLOYEE.getLabels()), EMPLOYEE.getProperties());
        resource.put(request, tx);
    }

    @Test
    public void testCanCreateNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("POST", "node", new ArrayList<>(ALICE.getLabels()), ALICE.getProperties());
        Node created = (Node)resource.post(request, tx);
        assert ALICE.equals(created);
        assert responseCollector.matchSingleResponse(Status2xx.CREATED, created);
    }

    @Test
    public void testCanDeleteExistingNode() throws Status4xx, Status5xx {
        Node created = database.createNode();
        FakeRequest request = new FakeRequest("DELETE", "node", created.getId());
        resource.delete(request, tx);
        assert responseCollector.matchSingleResponse(Status2xx.NO_CONTENT);
    }

    @Test(expected=NotFound.class)
    public void testCannotDeleteNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("DELETE", "node", 0);
        resource.delete(request, tx);
    }

}
