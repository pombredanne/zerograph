package org.zerograph.test;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.zerograph.test.helpers.FakeZerograph;
import org.zerograph.test.helpers.NodeSpec;
import org.zerograph.test.helpers.RelSpec;
import org.zerograph.test.helpers.ResponseCollector;

import java.util.List;
import java.util.Map;

public abstract class ResourceTest {

    final public static NodeSpec ALICE = NodeSpec.getAlice();
    final public static NodeSpec BOB = NodeSpec.getBob();
    final public static NodeSpec EMPLOYEE = NodeSpec.getEmployee();
    final public static NodeSpec ALICE_THE_EMPLOYEE = NodeSpec.getAliceTheEmployee();

    final public static RelSpec KNOWS_SINCE_1999 = RelSpec.getKnowsSince1999();
    final public static RelSpec KNOWS_FROM_WORK = RelSpec.getKnowsFromWork();
    final public static RelSpec KNOWS_SINCE_1999_FROM_WORK = RelSpec.getKnowsSince1999FromWork();

    protected FakeZerograph fakeZerograph;
    protected GraphDatabaseService database;
    protected ResponseCollector responseCollector;

    protected Transaction tx;

    @Before
    public void setUp() {
        fakeZerograph = new FakeZerograph();
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        responseCollector = new ResponseCollector();
        tx = database.beginTx();
    }

    @After
    public void tearDown() {
        tx.close();
        database.shutdown();
    }

    protected Node createNode() {
        return database.createNode();
    }

    protected Node createNode(NodeSpec spec) {
        Node created = database.createNode();
        addLabels(created, spec.getLabels());
        addProperties(created, spec.getProperties());
        return created;
    }

    protected Relationship createRel(NodeSpec start, RelSpec rel, NodeSpec end) {
        Node startNode = createNode(start);
        Node endNode = createNode(end);
        Relationship created = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName(rel.getType()));
        addProperties(created, rel.getProperties());
        return created;
    }

    public void addLabels(Node node, List<String> labelNames) {
        for (String labelName : labelNames) {
            node.addLabel(DynamicLabel.label(labelName));
        }
    }

    public void addProperties(PropertyContainer entity, Map<String, Object> properties) {
        for (String key : properties.keySet()) {
            entity.setProperty(key, properties.get(key));
        }
    }

}
