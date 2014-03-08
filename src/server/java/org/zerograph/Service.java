package org.zerograph;

import org.zeromq.ZMQ;

public abstract class Service implements Runnable {

    final public static int WORKER_COUNT = 40;

    final private ZMQ.Context context;
    final private Environment environment;
    final private String host;
    final private int port;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(String host, int port) {
        this.host = host;
        this.port = port;
        this.environment = Environment.getInstance();
        this.context = ZMQ.context(1);
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
        System.out.println("Starting service on " + this.port);
        this.internal = context.socket(ZMQ.DEALER);
        this.internal.bind(getInternalAddress());
        this.external = context.socket(ZMQ.ROUTER);
        this.external.bind(getExternalAddress());
        startWorkers(WORKER_COUNT);
        ZMQ.proxy(external, internal, null);
        shutdown();
    }

    public void shutdown() {
        System.out.println("Stopping service on " + this.port);
        external.close();
        internal.close();
        context.term();
    }

    public void stop() {
        // TODO: interrupt this thread and shut down gracefully
    }

}
