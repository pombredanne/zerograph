package org.zerograph.resource;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.NoContent;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeResource extends PropertyContainerResource implements TransactionalResourceInterface {

    final private static String NAME = "node";

    final private HashMap<String, Label> labelCache;

    public NodeResource(ZerographInterface zerograph, ResponderInterface responder, GraphDatabaseService database) {
        super(zerograph, responder, database);
        this.labelCache = new HashMap<>();
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public PropertyContainer get(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * PUT node {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            removeLabels(node);
            removeProperties(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * PATCH node {node_id} {labels} {properties}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            respond(new OK(node));
            return node;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    /**
     * POST node {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public PropertyContainer post(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        List labelNames = request.getListData(0);
        Map properties = request.getMapData(1);
        Node node = database().createNode();
        Lock writeLock = tx.acquireWriteLock(node);
        Lock readLock = tx.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        respond(new Created(node));
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            node.delete();
            writeLock.release();
            respond(new NoContent());
            return null;
        } catch (NotFoundException ex) {
            throw new NotFound("Node " + nodeID + " not found");
        }
    }

    public void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            node.addLabel(getLabel(labelName.toString()));
        }
    }

    public void removeLabels(Node node) {
        for (Label label : node.getLabels()) {
            node.removeLabel(label);
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

}
