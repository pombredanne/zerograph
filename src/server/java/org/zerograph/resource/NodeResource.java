package org.zerograph.resource;

import org.zerograph.Request;
import org.zerograph.Zerograph;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.NoContent;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status5xx.Abstract5xx;
import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeResource extends PropertyContainerResource implements TransactionalResourceInterface {

    final public static String NAME = "node";

    final private HashMap<String, Label> labelCache;

    public NodeResource(ZerographInterface zerograph, ZMQ.Socket socket, GraphDatabaseService database) {
        super(zerograph, socket, database);
        this.labelCache = new HashMap<>();
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public PropertyContainer get(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            send(new OK(node));
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
    public PropertyContainer put(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
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
            send(new OK(node));
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
    public PropertyContainer patch(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
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
            send(new OK(node));
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
    public PropertyContainer post(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        List labelNames = request.getListData(0);
        Map properties = request.getMapData(1);
        Node node = database().createNode();
        Lock writeLock = tx.acquireWriteLock(node);
        Lock readLock = tx.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        send(new Created(node));
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(Request request, Transaction tx) throws Abstract4xx, Abstract5xx {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            node.delete();
            writeLock.release();
            send(new NoContent());
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
