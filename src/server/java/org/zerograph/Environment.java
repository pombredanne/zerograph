package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.zerograph.api.ZerographInterface;

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
        String home = System.getenv("ZG_HOME");
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
                System.err.println("Cannot create directory " + directory);
                System.exit(1);  // TODO: throw exception instead
            }
        }
        return directory;
    }

    public synchronized GraphDatabaseService getDatabase(ZerographInterface zerograph, String host, int port) {
        if (databases.containsKey(port)) {
            return databases.get(port);
        } else {
            GraphDirectory directory = new GraphDirectory(zerograph, host, port);
            if (directory.exists()) {
                GraphDatabaseService database = factory.newEmbeddedDatabase(directory.getPath());
                databases.put(port, database);
                return database;
            } else {
                return null;
            }
        }
    }

    public synchronized GraphDatabaseService getOrCreateDatabase(ZerographInterface zerograph, String host, int port) {
        if (databases.containsKey(port)) {
            return databases.get(port);
        } else {
            GraphDirectory directory = new GraphDirectory(zerograph, host, port);
            GraphDatabaseService database = factory.newEmbeddedDatabase(directory.getPath());
            databases.put(port, database);
            return database;
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
