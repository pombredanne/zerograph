package org.zerograph.test.helpers;

import org.zerograph.Environment;
import org.zerograph.api.ZerographInterface;
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
}
