package com.nigelsmall.zerograph.resources;

import org.neo4j.graphdb.*;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;

public abstract class PropertyContainerResource extends Resource {

    public PropertyContainerResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    public static void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    public static void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
