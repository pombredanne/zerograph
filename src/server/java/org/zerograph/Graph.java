package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.zerograph.api.GraphInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;

import java.util.HashMap;

/**
 * A Graph service represents a database exposed over a server port.
 *
 */
public class Graph extends Service implements GraphInterface {

    final static private HashMap<Integer, Graph> instances = new HashMap<>(1);

    public static synchronized Graph startInstance(ZerographInterface zerograph, String host, int port, boolean create) throws GraphAlreadyStartedException, NoSuchGraphException {
        if (instances.containsKey(port)) {
            throw new GraphAlreadyStartedException(zerograph, host, port, instances.get(port));
        } else {
            Graph graph = new Graph(zerograph, host, port, create);
            Thread thread = new Thread(graph);
            try {
                thread.start();
            } catch (Exception ex) {
                throw new GraphAlreadyStartedException(zerograph, host, port, graph);
            }
            instances.put(port, graph);
            return graph;
        }
    }

    public static synchronized void stopInstance(ZerographInterface zerograph, String host, int port, boolean delete) throws GraphNotStartedException {
        // TODO: handle delete flag
        if (instances.containsKey(port)) {
            instances.get(port).stop();
        } else {
            throw new GraphNotStartedException(zerograph, host, port);
        }
        instances.remove(port);
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

}
