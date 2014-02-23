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
    final private HashMap<String, ExecutionEngine> engines;

    public Environment(String databaseHome) {
        this.context = ZMQ.context(1);
        this.databaseHome = databaseHome;
        this.databases = new HashMap<>();
        this.engines = new HashMap<>();
    }

    public ZMQ.Context getContext() {
        return this.context;
    }

    public String getDatabaseHome() {
        return this.databaseHome;
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

    public synchronized ExecutionEngine getEngine(String name) {
        GraphDatabaseService database = getDatabase(name);
        if (engines.containsKey(name)) {
            return engines.get(name);
        } else {
            ExecutionEngine engine = new ExecutionEngine(database);
            engines.put(name, engine);
            return engine;
        }
    }

}
