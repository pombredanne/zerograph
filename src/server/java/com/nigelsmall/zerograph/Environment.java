package com.nigelsmall.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zeromq.ZMQ;

import java.util.HashMap;

public class Environment {

    final private GraphDatabaseFactory factory = new GraphDatabaseFactory();

    final private ZMQ.Context context;
    final private String databaseHome;
    final private HashMap<Integer, GraphDatabaseService> databases;

    public Environment(String databaseHome) {
        this.context = ZMQ.context(1);
        this.databaseHome = databaseHome;
        this.databases = new HashMap<>();
    }

    public ZMQ.Context getContext() {
        return this.context;
    }

    public synchronized GraphDatabaseService getDatabase(int port) {
        if (databases.containsKey(port)) {
            return databases.get(port);
        } else {
            GraphDatabaseService database = factory.newEmbeddedDatabase(databaseHome + "/" + port);
            databases.put(port, database);
            return database;
        }
    }

}
