package org.zerograph.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.zerograph.test.helpers.QuickMap;
import org.zerograph.resources.RelResource;
import org.zerograph.Request;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;

public class RelationshipResourceTest extends ResourceTest {

    protected RelResource resource;

    protected Node alice;
    protected Node bob;

    @Before
    public void createResource() {
        resource = new RelResource(responseCollector);
        alice = createNode(ALICE);
        bob = createNode(BOB);
    }

    @Test
    public void testCanGetExistingRel() throws ClientError, ServerError {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        Request request = new Request("GET", "rel", QuickMap.from("id", created.getId()));
        Relationship got = (Relationship)resource.get(request, context);
        assert KNOWS_SINCE_1999.equals(got);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(created);
    }

    @Test(expected=ClientError.class)
    public void testCannotGetNonExistentRel() throws ClientError, ServerError {
        Request request = new Request("GET", "rel", QuickMap.from("id", 0));
        resource.get(request, context);
    }

    @Test
    public void testCanSetExistingRel() throws ClientError, ServerError {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        Request request = new Request("PUT", "rel",
                QuickMap.from("id", created.getId(), "properties", KNOWS_FROM_WORK.getProperties()));
        Relationship put = (Relationship)resource.set(request, context);
        assert KNOWS_FROM_WORK.equals(put);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(put);
    }

    @Test(expected=ClientError.class)
    public void testCannotSetNonExistentRel() throws ClientError, ServerError {
        Request request = new Request("PUT", "rel",
                QuickMap.from("id", 0, "properties", KNOWS_FROM_WORK.getProperties()));
        resource.set(request, context);
    }

    @Test
    public void testCanPatchExistingRel() throws ClientError, ServerError {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        Request request = new Request("PATCH", "rel",
                QuickMap.from("id", created.getId(), "properties", KNOWS_FROM_WORK.getProperties()));
        Relationship patched = (Relationship)resource.patch(request, context);
        assert KNOWS_SINCE_1999_FROM_WORK.equals(patched);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(patched);
    }

    @Test(expected=ClientError.class)
    public void testCannotPatchNonExistentRel() throws ClientError, ServerError {
        Request request = new Request("PATCH", "rel",
                QuickMap.from("id", 0, "properties", KNOWS_FROM_WORK.getProperties()));
        resource.patch(request, context);
    }

    @Test
    public void testCanCreateRel() throws ClientError, ServerError {
        Node alice = createNode(ALICE);
        Node bob = createNode(BOB);
        Request request = new Request("POST", "rel",
                QuickMap.from("start", alice.getId(), "end", bob.getId(), "type", KNOWS_SINCE_1999.getType(), "properties", KNOWS_SINCE_1999.getProperties()));
        Relationship created = (Relationship)resource.create(request, context);
        assert ALICE.equals(created.getStartNode());
        assert BOB.equals(created.getEndNode());
        assert KNOWS_SINCE_1999.equals(created);
        assert responseCollector.getBody().size() == 1;
        assert responseCollector.getBody().get(0).equals(created);
    }

    @Test
    public void testCanDeleteExistingRel() throws ClientError, ServerError {
        Relationship created = createRel(ALICE, KNOWS_SINCE_1999, BOB);
        Node alice = created.getStartNode();
        Node bob = created.getEndNode();
        Request request = new Request("DELETE", "rel", QuickMap.from("id", created.getId()));
        resource.delete(request, context);
        assert !alice.hasRelationship();
        assert !bob.hasRelationship();
        assert responseCollector.getBody().size() == 0;
    }

    @Test(expected=ClientError.class)
    public void testCannotDeleteNonExistentRel() throws ClientError, ServerError {
        Request request = new Request("DELETE", "rel", QuickMap.from("id", 0));
        resource.delete(request, context);
    }

}
