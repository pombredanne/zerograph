package org.zerograph.except;

import org.zerograph.service.api.ZerographInterface;

public class NoSuchGraphException extends GraphException {

    public NoSuchGraphException(ZerographInterface zerograph, String host, int port) {
        super(zerograph, host, port);
    }

}
