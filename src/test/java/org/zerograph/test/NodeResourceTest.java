package org.zerograph.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.resource.NodeResource;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public class NodeResourceTest extends ResourceTest {

    final public static NodeSpec ALICE = NodeSpec.getAlice();
    final public static NodeSpec EMPLOYEE = NodeSpec.getEmployee();
    final public static NodeSpec EMPLOYEE_ALICE = NodeSpec.getEmployeeAlice();

    protected NodeResource resource;

    @Before
    public void createResource() {
        resource = new NodeResource(fakeZerograph, responseCollector, database);
    }

    protected Node createNode() {
        return database.createNode();
    }

    protected Node createNode(NodeSpec spec) {
        Node created = database.createNode();
        resource.addLabels(created, spec.getLabels());
        resource.addProperties(created, spec.getProperties());
        return created;
    }

    @Test
    public void testCanGetExistingNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("GET", "node", 0);
        try (Transaction tx = database.beginTx()) {
            Node created = createNode(ALICE);
            assert created.getId() == 0;
            PropertyContainer got = resource.get(request, tx);
            assert got instanceof Node;
            assert ALICE.matches((Node) got);
            assert responseCollector.getResponseCount() == 1;
            assert responseCollector.matchResponse(0, 200, created);
        }
    }

    @Test
    public void testCannotGetNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("GET", "node", 0);
        try (Transaction tx = database.beginTx()) {
            try {
                resource.get(request, tx);
                assert false;
            } catch (Status4xx err) {
                assert true;
            }
            assert responseCollector.getResponseCount() == 0;
        }
    }

    @Test
    public void testCanPutExistingNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PUT", "node", 0, ALICE.getLabels(), ALICE.getProperties());
        try (Transaction tx = database.beginTx()) {
            Node created = createNode();
            assert created.getId() == 0;
            PropertyContainer got = resource.put(request, tx);
            assert got instanceof Node;
            Node gotNode = (Node)got;
            assert ALICE.matches(gotNode);
            assert responseCollector.getResponseCount() == 1;
            assert responseCollector.matchResponse(0, 200, gotNode);
        }
    }

    @Test
    public void testCannotPutNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PUT", "node", 0, ALICE.getLabels(), ALICE.getProperties());
        try (Transaction tx = database.beginTx()) {
            try {
                resource.put(request, tx);
                assert false;
            } catch (Status4xx err) {
                assert true;
            }
            assert responseCollector.getResponseCount() == 0;
        }
    }

    @Test
    public void testCanPatchExistingNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PATCH", "node", 0, EMPLOYEE.getLabels(), EMPLOYEE.getProperties());
        try (Transaction tx = database.beginTx()) {
            Node created = createNode(ALICE);
            assert created.getId() == 0;
            PropertyContainer got = resource.patch(request, tx);
            assert got instanceof Node;
            Node gotNode = (Node)got;
            assert EMPLOYEE_ALICE.matches(gotNode);
            assert responseCollector.getResponseCount() == 1;
            assert responseCollector.matchResponse(0, 200, gotNode);
        }
    }

    @Test
    public void testCannotPatchNonExistentNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PATCH", "node", 0, EMPLOYEE.getLabels(), EMPLOYEE.getProperties());
        try (Transaction tx = database.beginTx()) {
            try {
                resource.put(request, tx);
                assert false;
            } catch (Status4xx err) {
                assert true;
            }
            assert responseCollector.getResponseCount() == 0;
        }
    }

    @Test
    public void testCanCreateNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("POST", "node", ALICE.getLabels(), ALICE.getProperties());
        try (Transaction tx = database.beginTx()) {
            PropertyContainer created = resource.post(request, tx);
            assert created instanceof Node;
            Node createdNode = (Node)created;
            assert ALICE.matches(createdNode);
            assert responseCollector.getResponseCount() == 1;
            assert responseCollector.matchResponse(0, 201, createdNode);
        }
    }

    @Test
    public void testCanDeleteNode() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("DELETE", "node", 0);
        try (Transaction tx = database.beginTx()) {
            Node created = database.createNode();
            assert created.getId() == 0;
            resource.delete(request, tx);
        }
        try (Transaction tx = database.beginTx()) {
            try {
                database.getNodeById(0);
                assert false;
            } catch (NotFoundException ex) {
                assert true;
            }
            assert responseCollector.getResponseCount() == 1;
            assert responseCollector.matchResponse(0, 204);
        }
    }

}
