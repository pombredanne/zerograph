package org.zerograph.except;

public class GraphAlreadyStartedException extends GraphException {

    public GraphAlreadyStartedException(String host, int port) {
        super(host, port);
    }

}
