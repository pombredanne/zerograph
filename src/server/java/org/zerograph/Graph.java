package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServiceAlreadyStartedException;
import org.zerograph.except.ServiceNotStartedException;
import org.zerograph.worker.GraphWorker;

import java.util.HashMap;

public class Graph extends Service {

    final static private HashMap<Integer, Graph> instances = new HashMap<>(1);

    public static synchronized Graph getInstance(String host, int port) throws ClientError {
        if (instances.containsKey(port)) {
            return instances.get(port);
        } else {
            throw new ClientError(new Response(Response.NOT_FOUND, "No graph is listening on port " + port));
        }
    }

    public static synchronized Graph startInstance(String host, int port, boolean create) throws ServiceAlreadyStartedException {
        // TODO: handle create flag
        if (instances.containsKey(port)) {
            throw new ServiceAlreadyStartedException(port);
        } else {
            Graph service = new Graph(host, port);
            Thread thread = new Thread(service);
            try {
                thread.start();
            } catch (Exception ex) {
                throw new ServiceAlreadyStartedException(port);
            }
            instances.put(port, service);
            return service;
        }
    }

    public static synchronized void stopInstance(String host, int port, boolean delete) throws ServiceNotStartedException {
        // TODO: handle delete flag
        if (instances.containsKey(port)) {
            instances.get(port).stop();
        } else {
            throw new ServiceNotStartedException(port);
        }
        instances.remove(port);
    }

    final private GraphDatabaseService database;

    public Graph(String host, int port) {
        super(host, port);
        this.database = getEnvironment().getDatabase(port);
    }

    public GraphDatabaseService getDatabase() {
        return this.database;
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new GraphWorker(this)).start();
        }
    }

}
