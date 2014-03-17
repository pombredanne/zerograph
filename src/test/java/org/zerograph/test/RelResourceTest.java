package org.zerograph.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.zerograph.resource.RelResource;
import org.zerograph.response.status2xx.Status2xx;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;
import org.zerograph.test.helpers.FakeRequest;

public class RelResourceTest extends ResourceTest {

    protected RelResource resource;

    protected Node alice;
    protected Node bob;

    @Before
    public void createResource() {
        resource = new RelResource(fakeZerograph, responseCollector);
        alice = createNode(ALICE);
        bob = createNode(BOB);
    }

    @Test
    public void testCanGetExistingRel() throws Status4xx, Status5xx {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        FakeRequest request = new FakeRequest("GET", "rel", created.getId());
        Relationship got = (Relationship)resource.get(request, context);
        assert KNOWS_SINCE_1999.equals(got);
        assert responseCollector.matchSingleResponse(200, created);
    }

    @Test(expected=NotFound.class)
    public void testCannotGetNonExistentRel() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("GET", "rel", 0);
        resource.get(request, context);
    }

    @Test
    public void testCanPutExistingRel() throws Status4xx, Status5xx {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        FakeRequest request = new FakeRequest("PUT", "rel", created.getId(), KNOWS_FROM_WORK.getProperties());
        Relationship put = (Relationship)resource.put(request, context);
        assert KNOWS_FROM_WORK.equals(put);
        assert responseCollector.matchSingleResponse(Status2xx.OK, put);
    }

    @Test(expected=NotFound.class)
    public void testCannotPutNonExistentRel() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PUT", "rel", 0, KNOWS_FROM_WORK.getProperties());
        resource.put(request, context);
    }

    @Test
    public void testCanPatchExistingRel() throws Status4xx, Status5xx {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        FakeRequest request = new FakeRequest("PATCH", "rel", created.getId(), KNOWS_FROM_WORK.getProperties());
        Relationship patched = (Relationship)resource.patch(request, context);
        assert KNOWS_SINCE_1999_FROM_WORK.equals(patched);
        assert responseCollector.matchSingleResponse(Status2xx.OK, patched);
    }

    @Test(expected=NotFound.class)
    public void testCannotPatchNonExistentRel() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("PATCH", "rel", 0, KNOWS_FROM_WORK.getProperties());
        resource.patch(request, context);
    }

    @Test
    public void testCanCreateRel() throws Status4xx, Status5xx {
        Node alice = createNode(ALICE);
        Node bob = createNode(BOB);
        FakeRequest request = new FakeRequest("POST", "rel", alice.getId(), bob.getId(), KNOWS_SINCE_1999.getType(), KNOWS_SINCE_1999.getProperties());
        Relationship created = (Relationship)resource.post(request, context);
        assert ALICE.equals(created.getStartNode());
        assert BOB.equals(created.getEndNode());
        assert KNOWS_SINCE_1999.equals(created);
        assert responseCollector.matchSingleResponse(Status2xx.CREATED, created);
    }

    @Test
    public void testCanDeleteExistingRel() throws Status4xx, Status5xx {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        Node alice = created.getStartNode();
        Node bob = created.getEndNode();
        FakeRequest request = new FakeRequest("DELETE", "rel", created.getId());
        resource.delete(request, context);
        assert !alice.hasRelationship();
        assert !bob.hasRelationship();
        assert responseCollector.matchSingleResponse(Status2xx.NO_CONTENT);
    }

    @Test(expected=NotFound.class)
    public void testCannotDeleteNonExistentRel() throws Status4xx, Status5xx {
        FakeRequest request = new FakeRequest("DELETE", "rel", 0);
        resource.delete(request, context);
    }

}
