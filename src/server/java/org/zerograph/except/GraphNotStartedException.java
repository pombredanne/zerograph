package org.zerograph.except;

import org.zerograph.service.api.ZerographInterface;

public class GraphNotStartedException extends GraphException {

    public GraphNotStartedException(ZerographInterface zerograph, String host, int port) {
        super(zerograph, host, port);
    }

}
