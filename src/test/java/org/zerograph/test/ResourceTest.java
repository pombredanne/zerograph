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
import org.zerograph.api.NodeTemplateInterface;
import org.zerograph.api.RelationshipTemplateInterface;
import org.zerograph.Database;
import org.zerograph.test.helpers.ResponseCollector;
import org.zerograph.test.helpers.TestNodeTemplate;
import org.zerograph.test.helpers.TestRelationshipTemplate;

import java.util.Collection;
import java.util.Map;

public abstract class ResourceTest {

    final public static NodeTemplateInterface ALICE = TestNodeTemplate.getAlice();
    final public static NodeTemplateInterface BOB = TestNodeTemplate.getBob();
    final public static NodeTemplateInterface EMPLOYEE = TestNodeTemplate.getEmployee();
    final public static NodeTemplateInterface ALICE_THE_EMPLOYEE = TestNodeTemplate.getAliceTheEmployee();

    final public static RelationshipTemplateInterface KNOWS_SINCE_1999 = TestRelationshipTemplate.getKnowsSince1999();
    final public static RelationshipTemplateInterface KNOWS_FROM_WORK = TestRelationshipTemplate.getKnowsFromWork();
    final public static RelationshipTemplateInterface KNOWS_SINCE_1999_FROM_WORK = TestRelationshipTemplate.getKnowsSince1999FromWork();

    protected GraphDatabaseService database;
    protected ResponseCollector responseCollector;

    protected Transaction tx;
    protected Database context;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        responseCollector = new ResponseCollector();
        tx = database.beginTx();
        context = new Database(database, tx);
    }

    @After
    public void tearDown() {
        tx.close();
        database.shutdown();
    }

    protected Node createNode() {
        return database.createNode();
    }

    protected Node createNode(NodeTemplateInterface spec) {
        Node created = database.createNode();
        addLabels(created, spec.getLabels());
        addProperties(created, spec.getProperties());
        return created;
    }

    protected Relationship createRel(NodeTemplateInterface start, RelationshipTemplateInterface rel, NodeTemplateInterface end) {
        Node startNode = createNode(start);
        Node endNode = createNode(end);
        Relationship created = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName(rel.getType()));
        addProperties(created, rel.getProperties());
        return created;
    }

    public void addLabels(Node node, Collection<String> labelNames) {
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
