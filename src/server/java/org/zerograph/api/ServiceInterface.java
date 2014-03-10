package org.zerograph.api;

import org.zerograph.Environment;
import org.zeromq.ZMQ;

public interface ServiceInterface {

    public ZerographInterface getZerograph();

    public String getHost();

    public int getPort();

    public ZMQ.Context getContext();

    public Environment getEnvironment();

    public String getInternalAddress();

    public String getExternalAddress();

    public void start();

    public void stop();
}
