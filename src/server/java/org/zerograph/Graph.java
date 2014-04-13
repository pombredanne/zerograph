package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.zerograph.api.GraphInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.resources.CypherResource;
import org.zerograph.zapp.resources.GraphResource;
import org.zerograph.zapp.resources.NodeResource;
import org.zerograph.zapp.resources.NodeSetResource;
import org.zerograph.zapp.resources.RelationshipResource;
import org.zerograph.zapp.resources.RelationshipSetResource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Graph service represents a database exposed over a server port.
 *
 */
public class Graph extends Service implements GraphInterface {

    final static private HashMap<String, Graph> instances = new HashMap<>(1);

    private ArrayList<GraphWorker> workers;
    private ArrayList<Thread> threads;

    public static synchronized Graph get(String host, int port) {
        String key = Graph.key(host, port);
        return instances.get(key);
    }

    public static synchronized Graph open(String host, int port) throws GraphAlreadyStartedException {
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

    public static synchronized void close(String host, int port, boolean drop) throws GraphNotStartedException {
        String key = Graph.key(host, port);
        if (instances.containsKey(key)) {
            Graph graph = instances.remove(key);
            graph.stop();
            graph.getDatabase().shutdown();
            if (drop) {
                Environment.getInstance().dropDatabase(host, port);
            }
        } else {
            throw new GraphNotStartedException(host, port);
        }
    }

    final private GraphDatabaseService database;

    public Graph(String host, int port) {
        super(host, port);
        this.database = getEnvironment().getOrCreateDatabase(host, port);
        this.workers = new ArrayList<>(WORKER_COUNT);
        this.threads = new ArrayList<>(WORKER_COUNT);
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    @Override
    public void startWorkers() {
        System.out.println("Starting workers");
        for(int i = 0; i < WORKER_COUNT; i++) {
            GraphWorker worker = new GraphWorker(this);
            Thread thread = new Thread(worker);
            thread.setName(getPort() + "/" + worker.getUUID());
            workers.add(worker);
            threads.add(thread);
            thread.start();
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
        resourceSet.add(new RelationshipSetResource(responder));
        return resourceSet;
    }

}