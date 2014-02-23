package com.nigelsmall.zerograph;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zeromq.ZMQ;

import java.util.HashMap;

public class Environment {

    final private GraphDatabaseFactory factory = new GraphDatabaseFactory();

    final private ZMQ.Context context;
    final private String databaseHome;
    final private HashMap<String, GraphDatabaseService> databases;

    public Environment(String databaseHome) {
        this.context = ZMQ.context(1);
        this.databaseHome = databaseHome;
        this.databases = new HashMap<>();
    }

    public ZMQ.Context getContext() {
        return this.context;
    }

    public synchronized GraphDatabaseService getDatabase(String name) {
        if (databases.containsKey(name)) {
            return databases.get(name);
        } else {
            GraphDatabaseService database = factory.newEmbeddedDatabase(databaseHome + "/" + name);
            databases.put(name, database);
            return database;
        }
    }

}
