package org.zerograph.resource;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status2xx.Created;
import org.zerograph.response.status2xx.NoContent;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

import java.util.HashMap;
import java.util.Map;

public class RelResource extends PropertyContainerResource implements TransactionalResourceInterface {

    final private static String NAME = "rel";

    final private HashMap<String, RelationshipType> relationshipTypes;

    public RelResource(ZerographInterface zerograph, ResponderInterface responder, GraphDatabaseService database) {
        super(zerograph, responder, database);
        this.relationshipTypes = new HashMap<>();
    }

    public String getName() {
        return NAME;
    }

    /**
     * GET rel {rel_id}
     *
     * Fetch a single relationship by ID.
     */
    @Override
    public PropertyContainer get(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long relID = request.getIntegerData(0);
        try {
            Relationship rel = database().getRelationshipById(relID);
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * PUT rel {rel_id} {properties}
     *
     * Replace all properties on a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long relID = request.getIntegerData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            Lock readLock = tx.acquireReadLock(rel);
            removeProperties(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * PATCH rel {rel_id} {properties}
     *
     * Add new properties to a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist and any existing properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long relID = request.getIntegerData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            Lock readLock = tx.acquireReadLock(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            respond(new OK(rel));
            return rel;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    /**
     * POST rel {start_node} {end_node} {type} {properties}
     *
     * Create a new relationship.
     */
    @Override
    public PropertyContainer post(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        Node startNode = resolveNode(request.getData(0));
        Node endNode = resolveNode(request.getData(1));
        String typeName = request.getStringData(2);
        Map properties = request.getMapData(3);
        Relationship rel = startNode.createRelationshipTo(endNode, getRelationshipType(typeName));
        Lock writeLock = tx.acquireWriteLock(rel);
        Lock readLock = tx.acquireReadLock(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        respond(new Created(rel));
        return rel;
    }

    /**
     * DELETE rel {rel_id}
     *
     * Delete a relationship identified by ID.
     */
    @Override
    public PropertyContainer delete(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        long relID = request.getIntegerData(0);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            rel.delete();
            writeLock.release();
            respond(new NoContent());
            return null;
        } catch (NotFoundException ex) {
            throw new NotFound("Relationship " + relID + " not found");
        }
    }

    private Node resolveNode(Object value) throws Status4xx {
        if (value instanceof Node) {
            return (Node)value;
        } else if (value instanceof Integer) {
            try {
                return database().getNodeById((Integer)value);
            } catch (NotFoundException ex) {
                throw new NotFound("Relationship " + value + " not found");
            }
        } else {
            throw new BadRequest(value);
        }
    }

    private RelationshipType getRelationshipType(String name) {
        if (relationshipTypes.containsKey(name)) {
            return relationshipTypes.get(name);
        } else {
            DynamicRelationshipType relationshipType = DynamicRelationshipType.withName(name);
            relationshipTypes.put(name, relationshipType);
            return relationshipType;
        }
    }

}
