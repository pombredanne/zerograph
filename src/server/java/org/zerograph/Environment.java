package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zerograph.util.Log;
import org.zerograph.util.Toolbox;

import java.io.File;
import java.util.HashMap;

public class Environment {

    final private static String DEFAULT_HOST = "localhost";
    final private static int DEFAULT_PORT = 47470;

    final private static Environment instance = new Environment();
    final private static GraphDatabaseFactory factory = new GraphDatabaseFactory();

    private String host;
    private int port;
    private File homeDirectory;
    private File dataDirectory;
    private HashMap<Integer, GraphDatabaseService> databases;

    public static Environment getInstance() {
        return instance;
    }

    public Environment() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        this.setDirectories();
        this.databases = new HashMap<>();
    }

    private void setDirectories() {
        // set home directory
        String home = System.getenv("ZEROGRAPH_HOME");
        if (home == null) {
            String userName = System.getProperty("user.name");
            if ("root".equals(userName))
                home = "/var/zerograph";
            else
                home = System.getProperty("user.home") + "/" + ".zerograph";
        }
        this.homeDirectory = getOrCreateDirectory(home);
        // set data directory
        this.dataDirectory = getOrCreateDirectory(this.homeDirectory + "/" + "data");
    }

    private File getOrCreateDirectory(String path) {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                Log.write("Cannot create directory " + directory);
                System.exit(1);  // TODO: throw exception instead
            }
        }
        return directory;
    }

    public synchronized GraphDatabaseService openDatabase(String host, int port) {
        if (databases.containsKey(port)) {
            return databases.get(port);
        } else {
            GraphDirectory directory = new GraphDirectory(host, port);
            GraphDatabaseService database = factory.newEmbeddedDatabase(directory.getPath());
            databases.put(port, database);
            return database;
        }
    }

    public synchronized void dropDatabase(String host, int port) {
        if (databases.containsKey(port)) {
            Log.write("Shutting down database " + host + ":" + port);
            databases.get(port).shutdown();
            GraphDirectory directory = new GraphDirectory(host, port);
            Log.write("Deleting database directory " + host + ":" + port);
            Toolbox.delete(directory.getFile());
            databases.remove(port);
            Log.write("Database deleted");
        } else {
            throw new IllegalArgumentException("No such database");
        }
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public File getHomeDirectory() {
        return this.homeDirectory;
    }

    public File getDataDirectory() {
        return this.dataDirectory;
    }

}
