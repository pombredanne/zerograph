package org.zerograph.except;

public class ServiceNotStartedException extends ServiceException {

    public ServiceNotStartedException(int port) {
        super(port);
    }

}
