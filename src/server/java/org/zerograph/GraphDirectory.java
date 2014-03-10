package org.zerograph;

import org.zerograph.api.ZerographInterface;

import java.io.File;

/**
 * A graph database on disk. This may or may not exist as a file, unlike a
 * Graph which must exist on disk.
 *
 */
public class GraphDirectory {

    final private ZerographInterface zerograph;
    final private String host;
    final private int port;
    final private File file;

    public GraphDirectory(ZerographInterface zerograph, String host, int port) {
        this.zerograph = zerograph;
        this.host = host;
        this.port = port;
        this.file = new File(zerograph.getEnvironment().getDataDirectory().getPath() + "/" + port);
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
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

    public boolean exists() {
        return this.file.exists();
    }

}
