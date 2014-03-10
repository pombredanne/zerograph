package org.zerograph.api;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public interface TransactionalResourceInterface {

    public String getName();

    public PropertyContainer get(RequestInterface request, Transaction tx) throws Status4xx, Status5xx;

    public PropertyContainer put(RequestInterface request, Transaction tx) throws Status4xx, Status5xx;

    public PropertyContainer patch(RequestInterface request, Transaction tx) throws Status4xx, Status5xx;

    public PropertyContainer post(RequestInterface request, Transaction tx) throws Status4xx, Status5xx;

    public PropertyContainer delete(RequestInterface request, Transaction tx) throws Status4xx, Status5xx;

}
