package org.zerograph.except;

public class ServiceAlreadyStartedException extends ServiceException {

    public ServiceAlreadyStartedException(int port) {
        super(port);
    }

}
