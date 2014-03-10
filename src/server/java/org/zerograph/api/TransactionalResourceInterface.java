package org.zerograph.api;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status5xx.Abstract5xx;

public interface TransactionalResourceInterface extends ResourceInterface {

    public PropertyContainer get(Request request, Transaction tx) throws Abstract4xx, Abstract5xx;

    public PropertyContainer put(Request request, Transaction tx) throws Abstract4xx, Abstract5xx;

    public PropertyContainer patch(Request request, Transaction tx) throws Abstract4xx, Abstract5xx;

    public PropertyContainer post(Request request, Transaction tx) throws Abstract4xx, Abstract5xx;

    public PropertyContainer delete(Request request, Transaction tx) throws Abstract4xx, Abstract5xx;

}
