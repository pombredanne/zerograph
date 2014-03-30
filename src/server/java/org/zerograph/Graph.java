package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.api.GraphInterface;
import org.zerograph.zap.CypherResource;
import org.zerograph.zap.GraphResource;
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

    public static synchronized Graph getInstance(String host, int port) {
        String key = Graph.key(host, port);
        return instances.get(key);
    }

    public static synchronized Graph setInstance(String host, int port) throws GraphAlreadyStartedException {
        String key = Graph.key(host, port);
        if (instances.containsKey(key)) {
            throw new GraphAlreadyStartedException(host, port, instances.get(key));
        } else {
            Graph graph = new Graph(host, port);
            Thread thread = new Thread(graph);
            try {
                thread.start();
            } catch (Exception ex) {
                throw new GraphAlreadyStartedException(host, port, graph);
            }
            instances.put(key, graph);
            return graph;
        }
    }

    public static synchronized void stopInstance(String host, int port, boolean delete) throws GraphNotStartedException {
        // TODO: handle delete flag
        String key = Graph.key(host, port);
        if (instances.containsKey(key)) {
            instances.get(key).stop();
        } else {
            throw new GraphNotStartedException(host, port);
        }
        instances.remove(key);
    }

    final private GraphDatabaseService database;

    public Graph(String host, int port) {
        super(host, port);
        this.database = getEnvironment().getOrCreateDatabase(host, port);
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new GraphWorker(this)).start();
        }
    }

    @Override
    public ResourceSet createResourceSet(ResponderInterface responder) {
        ResourceSet resourceSet = new ResourceSet();
        resourceSet.add(new GraphResource(responder));
        resourceSet.add(new CypherResource(responder));
        resourceSet.add(new NodeResource(responder));
        resourceSet.add(new NodeSetResource(responder));
        resourceSet.add(new RelationshipResource(responder));
        return resourceSet;
    }

}
