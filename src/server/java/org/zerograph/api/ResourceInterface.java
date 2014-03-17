package org.zerograph.api;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public interface ResourceInterface {

    public String getName();

    public PropertyContainer get(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx;

    public PropertyContainer put(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx;

    public PropertyContainer patch(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx;

    public PropertyContainer post(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx;

    public PropertyContainer delete(RequestInterface request, Neo4jContextInterface context) throws Status4xx, Status5xx;

}
