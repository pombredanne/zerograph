package org.zerograph.test.helpers;

import org.zerograph.Environment;
import org.zerograph.Graph;
import org.zerograph.ResourceSet;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;
import org.zeromq.ZMQ;

public class FakeZerograph implements ZerographInterface {

    @Override
    public ZerographInterface getZerograph() {
        return null;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public ZMQ.Context getContext() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public String getInternalAddress() {
        return null;
    }

    @Override
    public String getExternalAddress() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Graph getGraph(String host, int port) throws NoSuchGraphException {
        return null;
    }

    @Override
    public Graph startGraph(String host, int port, boolean create) throws NoSuchGraphException, GraphAlreadyStartedException {
        return null;
    }

    @Override
    public void stopGraph(String host, int port, boolean delete) throws GraphNotStartedException {

    }

    @Override
    public ResourceSet createResourceSet(ResponderInterface responder) {
        return null;
    }
}
