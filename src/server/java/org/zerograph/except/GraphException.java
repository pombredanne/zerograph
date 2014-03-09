package org.zerograph.except;

public class GraphException extends Exception {

    final private String host;
    final private int port;

    public GraphException(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

}
