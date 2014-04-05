package org.zerograph;

import org.zerograph.Environment;

import java.io.File;

/**
 * A graph database on disk. This may or may not exist as a file, unlike a
 * Graph which must exist on disk.
 *
 */
public class GraphDirectory {

    final private String host;
    final private int port;
    final private File file;

    public GraphDirectory(String host, int port) {
        this.host = host;
        this.port = port;
        this.file = new File(Environment.getInstance().getDataDirectory().getPath() + "/" + port);
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.file.getPath();
    }

    public File getFile() {
        return this.file;
    }

    public boolean exists() {
        return this.file.exists();
    }

}
