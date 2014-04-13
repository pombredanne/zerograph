package org.zerograph;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
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
import org.neo4j.tooling.GlobalGraphOperations;
import org.zerograph.api.DatabaseInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Database implements DatabaseInterface {

    final private GraphDatabaseService database;
    final private ExecutionEngine engine;
    final private GlobalGraphOperations global;
    final private Transaction transaction;

    final private HashMap<String, Label> labelCache;
    final private HashMap<String, RelationshipType> typeCache;

    public Database(GraphDatabaseService database, Transaction transaction) {
        this.database = database;
        this.engine = new ExecutionEngine(database);
        this.global = GlobalGraphOperations.at(database);
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
    public Iterable<Node> matchNodeSet(String label, String key, Object value) {
        if (key == null) {
            return global.getAllNodesWithLabel(getLabel(label));
        } else {
            return new IterableResult<>(database.findNodesByLabelAndProperty(getLabel(label), key, value));
        }
    }

    @Override
    public Iterable<Node> mergeNodeSet(String label, String key, Object value) {
        String query = "MERGE (n:`" + label.replace("`", "``") +
                "` {`" + key.replace("`", "``") + "`:{value}}) RETURN n";
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("value", value);
        IterableExecutor<Node> executor = new IterableExecutor<>(engine);
        return executor.execute(query, params, "n");
    }

    @Override
    public Iterable<Node> purgeNodeSet(String label, String key, Object value) {
        for (Node node : database.findNodesByLabelAndProperty(getLabel(label), key, value)) {
            node.delete();
        }
        return null;
    }

    @Override
    public Iterable<Relationship> matchRelationshipSet(Node startNode, Node endNode, String type) {
        if (startNode != null && endNode != null) {
            String query;
            if (type == null) {
                query = "START a=node({a}), b=node({b}) MATCH (a)-[ab]->(b) RETURN ab";
            } else {
                query = "START a=node({a}), b=node({b}) MATCH (a)-[ab:`" + type + "`]->(b) RETURN ab";
            }
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("a", startNode.getId());
            params.put("b", endNode.getId());
            IterableExecutor<Relationship> executor = new IterableExecutor<>(engine);
            return executor.execute(query, params, "ab");
        } else if (startNode != null) {
            if (type == null) {
                return startNode.getRelationships(Direction.OUTGOING);
            } else {
                return startNode.getRelationships(Direction.OUTGOING, getRelationshipType(type));
            }
        } else if (endNode != null) {
            if (type == null) {
                return endNode.getRelationships(Direction.INCOMING);
            } else {
                return endNode.getRelationships(Direction.INCOMING, getRelationshipType(type));
            }
        } else {
            throw new IllegalArgumentException("Either start or end nodes must be specified");
        }
    }

    @Override
    public Iterable<Relationship> mergeRelationshipSet(Node startNode, Node endNode, String type) {
        String query = "START a=node({a}), b=node({b}) MERGE (a)-[ab:`" + type + "`]->(b) RETURN ab";
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("a", startNode.getId());
        params.put("b", endNode.getId());
        IterableExecutor<Relationship> executor = new IterableExecutor<>(engine);
        return executor.execute(query, params, "ab");
    }

    @Override
    public Iterable<Relationship> purgeRelationshipSet(Node startNode, Node endNode, String type) {
        if (startNode != null && endNode != null) {
            String query;
            if (type == null) {
                query = "START a=node({a}), b=node({b}) MATCH (a)-[ab]->(b) DELETE ab";
            } else {
                query = "START a=node({a}), b=node({b}) MATCH (a)-[ab:`" + type + "`]->(b) DELETE ab";
            }
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("a", startNode.getId());
            params.put("b", endNode.getId());
            engine.execute(query, params);
        } else if (startNode != null) {
            Iterable<Relationship> rels;
            if (type == null) {
                rels = startNode.getRelationships(Direction.OUTGOING);
            } else {
                rels = startNode.getRelationships(Direction.OUTGOING, getRelationshipType(type));
            }
            for (Relationship rel : rels) {
                rel.delete();
            }
        } else if (endNode != null) {
            Iterable<Relationship> rels;
            if (type == null) {
                rels = endNode.getRelationships(Direction.INCOMING);
            } else {
                rels = endNode.getRelationships(Direction.INCOMING, getRelationshipType(type));
            }
            for (Relationship rel : rels) {
                rel.delete();
            }
        } else {
            throw new IllegalArgumentException("Either start or end nodes must be specified");
        }
        return new ArrayList<>();
    }


    // PRIVATE METHODS BELOW HERE

    private void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            if (labelName != null) {
                node.addLabel(getLabel(labelName.toString()));
            }
        }
    }

    private void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            String keyString = key.toString();
            Object value = properties.get(key);
            if (value instanceof List) {
                List listValue = (List) value;
                int listValueSize = listValue.size();
                if (listValueSize >= 1) {
                    Object firstItem = listValue.get(0);
                    try {
                        if (firstItem instanceof Boolean) {
                            entity.setProperty(keyString, listValue.toArray(new Boolean[listValueSize]));
                        } else if (firstItem instanceof Integer) {
                            entity.setProperty(keyString, listValue.toArray(new Integer[listValueSize]));
                        } else if (firstItem instanceof Long) {
                            entity.setProperty(keyString, listValue.toArray(new Long[listValueSize]));
                        } else if (firstItem instanceof Double) {
                            entity.setProperty(keyString, listValue.toArray(new Double[listValueSize]));
                        } else if (firstItem instanceof String) {
                            entity.setProperty(keyString, listValue.toArray(new String[listValueSize]));
                        } else {
                            throw new ClassCastException("Cannot cast List property to a supported type");
                        }
                    } catch (ArrayStoreException ex) {
                        throw new ClassCastException("Cannot cast List property to a supported type");
                    }
                } else {
                    entity.setProperty(keyString, new String[0]);
                }
            } else {
                entity.setProperty(keyString, value);
            }
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
