package org.zerograph.zap.api;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

public interface ResourceInterface {

    public String getName();

    public PropertyContainer get(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

    public PropertyContainer set(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

    public PropertyContainer patch(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

    public PropertyContainer create(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

    public PropertyContainer delete(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

    public PropertyContainer execute(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError;

}
