package org.zerograph.api;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public interface ResourceInterface {

    public String getName();

    public PropertyContainer get(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx;

    public PropertyContainer put(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx;

    public PropertyContainer patch(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx;

    public PropertyContainer post(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx;

    public PropertyContainer delete(Neo4jContextInterface context, RequestInterface request) throws Status4xx, Status5xx;

}
