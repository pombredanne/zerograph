package org.zerograph.resource;

import org.neo4j.graphdb.*;
import org.zerograph.Zerograph;
import org.zeromq.ZMQ;

import java.util.Map;

public abstract class BasePropertyContainerResource extends BaseGraphResource {

    public BasePropertyContainerResource(Zerograph zerograph, ZMQ.Socket socket, GraphDatabaseService database) {
        super(zerograph, socket, database);
    }

    public void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    public void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
