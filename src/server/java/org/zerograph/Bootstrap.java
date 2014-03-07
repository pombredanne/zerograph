package org.zerograph;

import org.zerograph.except.ServiceAlreadyRunningException;

public class Bootstrap {

    public static void main(String[] args) {
        // TODO: add shutdown hook
        try {
            Service.start(47474);
        } catch (ServiceAlreadyRunningException ex) {
            System.err.println("A service is already running on port 47474");
            System.exit(1);
        }
    }

}
