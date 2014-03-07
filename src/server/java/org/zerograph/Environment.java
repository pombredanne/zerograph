package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class Environment {

    final private static Environment instance = new Environment();

    final private GraphDatabaseFactory factory = new GraphDatabaseFactory();

    final private File storagePath;
    final private HashMap<Integer, GraphDatabaseService> databases;

    public static Environment getInstance() {
        return instance;
    }

    public Environment() {
        this.storagePath = new File(getStoragePath());
        this.databases = new HashMap<>();
        if (!this.storagePath.isDirectory()) {
            if (!this.storagePath.mkdirs()) {
                System.err.println("Cannot create storage path " + this.storagePath);
                System.exit(1);
            }
        }
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

    public static String getStoragePath() {
        String storagePath = System.getenv("ZG_STORAGE_PATH");
        if (storagePath != null)
            return storagePath;
        String userName = System.getProperty("user.name");
        if ("root".equals(userName))
            return "/var/zerograph";
        else
            return System.getProperty("user.home") + "/" + ".zerograph";
    }

}
