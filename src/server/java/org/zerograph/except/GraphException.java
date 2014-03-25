package org.zerograph.except;

import org.zerograph.service.api.ZerographInterface;

public class GraphException extends ZerographException {

    final private ZerographInterface zerograph;
    final private String host;
    final private int port;

    public GraphException(ZerographInterface zerograph, String host, int port) {
        this.zerograph = zerograph;
        this.host = host;
        this.port = port;
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

}
