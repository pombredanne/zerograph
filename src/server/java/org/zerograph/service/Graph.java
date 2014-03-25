package org.zerograph.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.zerograph.ResourceSet;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zerograph.service.api.GraphInterface;
import org.zerograph.service.api.ZerographInterface;
import org.zerograph.zap.CypherResource;
import org.zerograph.zap.NodeResource;
import org.zerograph.zap.NodeSetResource;
import org.zerograph.zap.RelationshipResource;
import org.zerograph.zpp.api.ResponderInterface;

import java.util.HashMap;

/**
 * A Graph service represents a database exposed over a server port.
 *
 */
public class Graph extends Service implements GraphInterface {

    final static private HashMap<String, Graph> instances = new HashMap<>(1);

    public static synchronized Graph getInstance(ZerographInterface zerograph, String host, int port) {
        String key = Graph.key(host, port);
        return instances.get(key);
    }

    public static synchronized Graph startInstance(ZerographInterface zerograph, String host, int port, boolean create) throws NoSuchGraphException, GraphAlreadyStartedException {
        String key = Graph.key(host, port);
        if (instances.containsKey(key)) {
            throw new GraphAlreadyStartedException(zerograph, host, port, instances.get(key));
        } else {
            Graph graph = new Graph(zerograph, host, port, create);
            Thread thread = new Thread(graph);
            try {
                thread.start();
            } catch (Exception ex) {
                throw new GraphAlreadyStartedException(zerograph, host, port, graph);
            }
            instances.put(key, graph);
            return graph;
        }
    }

    public static synchronized void stopInstance(ZerographInterface zerograph, String host, int port, boolean delete) throws GraphNotStartedException {
        // TODO: handle delete flag
        String key = Graph.key(host, port);
        if (instances.containsKey(key)) {
            instances.get(key).stop();
        } else {
            throw new GraphNotStartedException(zerograph, host, port);
        }
        instances.remove(key);
    }

    final private GraphDatabaseService database;

    public Graph(ZerographInterface zerograph, String host, int port, boolean create) throws NoSuchGraphException {
        super(zerograph, host, port);
        if (create) {
            this.database = getEnvironment().getOrCreateDatabase(zerograph, host, port);
        } else {
            this.database = getEnvironment().getDatabase(zerograph, host, port);
        }
        if (this.database == null) {
            throw new NoSuchGraphException(zerograph, host, port);
        }
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new GraphWorker(getZerograph(), this)).start();
        }
    }

    @Override
    public ResourceSet createResourceSet(ResponderInterface responder) {
        ResourceSet resourceSet = new ResourceSet();
        resourceSet.add(new CypherResource(zerograph, responder));
        resourceSet.add(new NodeResource(zerograph, responder));
        resourceSet.add(new NodeSetResource(zerograph, responder));
        resourceSet.add(new RelationshipResource(zerograph, responder));
        return resourceSet;
    }

}
