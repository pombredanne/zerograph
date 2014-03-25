package org.zerograph.neo4j.api;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.zerograph.IterableResult;

import java.util.List;
import java.util.Map;

public interface DatabaseInterface {

    public ExecutionResult execute(String query) throws CypherException;

    public ExecutionResult execute(String query, Map<String, Object> params) throws CypherException;

    public ExecutionResult profile(String query, Map<String, Object> params) throws CypherException;

    public Node getNode(long id) throws NotFoundException;

    public Node putNode(long id, List labelNames, Map properties) throws NotFoundException;

    public Node patchNode(long id, List labelNames, Map properties) throws NotFoundException;

    public Node createNode(List labelNames, Map properties);

    public void deleteNode(long id) throws NotFoundException;

    public Relationship getRelationship(long id) throws NotFoundException;

    public Relationship putRelationship(long id, Map properties) throws NotFoundException;

    public Relationship patchRelationship(long id, Map properties) throws NotFoundException;

    public Relationship createRelationship(Node startNode, Node endNode, String type, Map properties);

    public void deleteRelationship(long id) throws NotFoundException;

    public IterableResult<Node> matchNodeSet(String label, String key, Object value);

    public IterableResult<Node> mergeNodeSet(String label, String key, Object value);

    public IterableResult<Node> purgeNodeSet(String label, String key, Object value);

}
