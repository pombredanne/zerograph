package org.zerograph;

import java.io.File;

/**
 * A graph database on disk. This may or may not exist as a file, unlike a
 * Graph which must exist on disk.
 *
 */
public class GraphDirectory {

    final private Zerograph zerograph;
    final private String host;
    final private int port;
    final private File file;

    public GraphDirectory(Zerograph zerograph, String host, int port) {
        this.zerograph = zerograph;
        this.host = host;
        this.port = port;
        this.file = new File(zerograph.getEnvironment().getDataDirectory().getPath() + "/" + port);
    }

    public Zerograph getZerograph() {
        return this.zerograph;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean exists() {
        return this.file.exists();
    }

}
