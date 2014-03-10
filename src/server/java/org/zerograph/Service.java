package org.zerograph;

import org.zerograph.api.ServiceInterface;
import org.zerograph.api.ZerographInterface;
import org.zeromq.ZMQ;

public abstract class Service implements Runnable, ServiceInterface {

    final public static int WORKER_COUNT = 40;

    final private ZerographInterface zerograph;
    final private String host;
    final private int port;

    final private ZMQ.Context context;
    final private Environment environment;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(ZerographInterface zerograph, String host, int port) {
        this.zerograph = zerograph;
        this.host = host;
        this.port = port;
        this.environment = Environment.getInstance();
        this.context = ZMQ.context(1);
    }

    public ZerographInterface getZerograph() {
        return this.zerograph;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public ZMQ.Context getContext() {
        return context;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public String getInternalAddress() {
        return "inproc://" + host + "-" + port;
    }

    public String getExternalAddress() {
        return "tcp://" + host + ":" + port;
    }

    public abstract void startWorkers(int count);

    public void run() {
        start();
        stop();
    }

    public void start() {
        System.out.println("Starting service on " + this.port);
        this.internal = context.socket(ZMQ.DEALER);
        this.internal.bind(getInternalAddress());
        this.external = context.socket(ZMQ.ROUTER);
        this.external.bind(getExternalAddress());
        startWorkers(WORKER_COUNT);
        ZMQ.proxy(external, internal, null);
    }

    public void stop() {
        System.out.println("Stopping service on " + this.port);
        // TODO: interrupt this thread and shut down gracefully
        external.close();
        internal.close();
        context.term();
    }

}
