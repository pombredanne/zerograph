package org.zerograph;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.zerograph.IterableResult;
import org.zerograph.Statistics;
import org.zerograph.api.DatabaseInterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Database implements DatabaseInterface {

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;
    final private Transaction transaction;

    final private HashMap<String, Label> labelCache;
    final private HashMap<String, RelationshipType> typeCache;

    public Database(GraphDatabaseService database, Transaction transaction) {
        this.database = database;
        this.engine = new ExecutionEngine(database);
        this.transaction = transaction;
        this.labelCache = new HashMap<>();
        this.typeCache = new HashMap<>();
    }

    @Override
    public ExecutionResult execute(String query) throws CypherException {
        return engine.execute(query);
    }

    @Override
    public ExecutionResult execute(String query, Map<String, Object> params) throws CypherException {
        return engine.execute(query, params);
    }

    @Override
    public ExecutionResult profile(String query, Map<String, Object> params) throws CypherException {
        return engine.profile(query, params);
    }

    @Override
    public Node getNode(long id) throws NotFoundException {
        return database.getNodeById(id);
    }

    @Override
    public Node putNode(long id, List labelNames, Map properties) throws NotFoundException {
        Node node = database.getNodeById(id);
        Lock writeLock = transaction.acquireWriteLock(node);
        Lock readLock = transaction.acquireReadLock(node);
        removeLabels(node);
        removeProperties(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        return node;
    }

    @Override
    public Node patchNode(long id, List labelNames, Map properties) throws NotFoundException {
        Node node = database.getNodeById(id);
        Lock writeLock = transaction.acquireWriteLock(node);
        Lock readLock = transaction.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        return node;
    }

    @Override
    public Node createNode(List labelNames, Map properties) {
        Node node = database.createNode();
        Lock writeLock = transaction.acquireWriteLock(node);
        Lock readLock = transaction.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        return node;
    }

    @Override
    public void deleteNode(long id) throws NotFoundException {
        Node node = database.getNodeById(id);
        Lock writeLock = transaction.acquireWriteLock(node);
        node.delete();
        writeLock.release();
    }

    @Override
    public Relationship getRelationship(long id) throws NotFoundException {
        return database.getRelationshipById(id);
    }

    @Override
    public Relationship putRelationship(long id, Map properties) throws NotFoundException {
        Relationship rel = database.getRelationshipById(id);
        Lock writeLock = transaction.acquireWriteLock(rel);
        Lock readLock = transaction.acquireReadLock(rel);
        removeProperties(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        return rel;
    }

    @Override
    public Relationship patchRelationship(long id, Map properties) throws NotFoundException {
        Relationship rel = database.getRelationshipById(id);
        Lock writeLock = transaction.acquireWriteLock(rel);
        Lock readLock = transaction.acquireReadLock(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        return rel;
    }

    @Override
    public Relationship createRelationship(Node startNode, Node endNode, String type, Map properties) {
        Relationship rel = startNode.createRelationshipTo(endNode, getRelationshipType(type));
        Lock writeLock = transaction.acquireWriteLock(rel);
        Lock readLock = transaction.acquireReadLock(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        return rel;
    }

    @Override
    public void deleteRelationship(long id) throws NotFoundException {
        Relationship rel = database.getRelationshipById(id);
        Lock writeLock = transaction.acquireWriteLock(rel);
        rel.delete();
        writeLock.release();
    }

    @Override
    public IterableResult<Node> matchNodeSet(String label, String key, Object value) {
        return new IterableResult<>(database.findNodesByLabelAndProperty(getLabel(label), key, value));
    }

    @Override
    public IterableResult<Node> mergeNodeSet(String label, String key, Object value) {
        String query = "MERGE (n:`" + label.replace("`", "``") +
                "` {`" + key.replace("`", "``") + "`:{value}}) RETURN n";
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("value", value);
        final ExecutionResult result = execute(query, params);

        return new IterableResult<Node>() {

            final Iterator<Map<String, Object>> resultIterator = result.iterator();

            @Override
            public Statistics getStatistics() {
                return new Statistics(result.getQueryStatistics());
            }

            @Override
            public Iterator<Node> iterator() {

                return new Iterator<Node>() {

                    public boolean hasNext() {
                        return resultIterator.hasNext();
                    }

                    public Node next() {
                        Map<String, Object> row = resultIterator.next();
                        Object value = row.get("n");
                        if (value instanceof Node) {
                            Node nodeValue = (Node) value;
                            if (getFirst() == null) {
                                setFirst(nodeValue);
                            }
                            return nodeValue;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };

            }

        };
    }

    @Override
    public IterableResult<Node> purgeNodeSet(String label, String key, Object value) {
        for (Node node : database.findNodesByLabelAndProperty(getLabel(label), key, value)) {
            node.delete();
        }
        return null;
    }


    // PRIVATE METHODS BELOW HERE

    private void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            node.addLabel(getLabel(labelName.toString()));
        }
    }

    private void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    private Label getLabel(String name) {
        if (labelCache.containsKey(name)) {
            return labelCache.get(name);
        } else {
            Label label = DynamicLabel.label(name);
            labelCache.put(name, label);
            return label;
        }
    }

    private RelationshipType getRelationshipType(String name) {
        if (typeCache.containsKey(name)) {
            return typeCache.get(name);
        } else {
            DynamicRelationshipType relationshipType = DynamicRelationshipType.withName(name);
            typeCache.put(name, relationshipType);
            return relationshipType;
        }
    }

    private void removeLabels(Node node) {
        for (Label label : node.getLabels()) {
            node.removeLabel(label);
        }
    }

    private void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
