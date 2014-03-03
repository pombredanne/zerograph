package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class Environment {

    final private GraphDatabaseFactory factory = new GraphDatabaseFactory();

    final private ZMQ.Context context;
    final private File storagePath;
    final private HashMap<Integer, GraphDatabaseService> databases;

    public Environment(String storagePath) throws FileNotFoundException {
        this.context = ZMQ.context(1);
        this.storagePath = new File(storagePath);
        this.databases = new HashMap<>();
        if (!this.storagePath.isDirectory()) {
            if (!this.storagePath.mkdirs()) {
                throw new FileNotFoundException("Cannot create storage path " +
                        this.storagePath);
            }
        }
    }

    public ZMQ.Context getContext() {
        return this.context;
    }

    public synchronized GraphDatabaseService getDatabase(int port) {
        if (databases.containsKey(port)) {
            return databases.get(port);
        } else {
            GraphDatabaseService database = factory.newEmbeddedDatabase(storagePath + "/" + port);
            databases.put(port, database);
            return database;
        }
    }

}
