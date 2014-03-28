package org.zerograph.except;

public class NoSuchGraphException extends GraphException {

    public NoSuchGraphException(String host, int port) {
        super(host, port);
    }

}
