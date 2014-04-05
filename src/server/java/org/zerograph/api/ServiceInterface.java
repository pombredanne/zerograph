package org.zerograph.api;

import org.zerograph.Environment;
import org.zerograph.ResourceSet;
import org.zerograph.zpp.api.ResponderInterface;
import org.zeromq.ZMQ;

public interface ServiceInterface {

    public String getHost();

    public int getPort();

    public Environment getEnvironment();

    public String getInternalAddress();

    public String getExternalAddress();

    public ZMQ.Context getContext();

    public void start();

    public void stop();

    public ResourceSet createResourceSet(ResponderInterface responder);

}
