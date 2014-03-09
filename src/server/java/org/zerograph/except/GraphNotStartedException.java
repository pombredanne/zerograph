package org.zerograph.except;

public class GraphNotStartedException extends GraphException {

    public GraphNotStartedException(String host, int port) {
        super(host, port);
    }

}
