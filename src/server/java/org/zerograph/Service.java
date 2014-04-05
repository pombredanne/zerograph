package org.zerograph;

import org.zerograph.api.ServiceInterface;
import org.zeromq.ZMQ;
import zmq.ZError;

import java.nio.channels.ClosedChannelException;

public abstract class Service implements Runnable, ServiceInterface {

    public static String key(String host, int port) {
        return host + ":" + port;
    }

    final public static int WORKER_COUNT = 40;

    final private String host;
    final private int port;

    final private Environment environment;

    private ZMQ.Context context;
    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(String host, int port) {
        this.host = host;
        this.port = port;
        this.environment = Environment.getInstance();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
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

    public ZMQ.Context getContext() {
        return context;
    }

    public abstract void startWorkers();

    public void run() {
        start();
    }

    public void start() {
        System.out.println("Starting service on " + this.port);
        this.context = ZMQ.context(1);
        this.internal = context.socket(ZMQ.DEALER);
        this.internal.bind(getInternalAddress());
        this.external = context.socket(ZMQ.ROUTER);
        this.external.bind(getExternalAddress());
        startWorkers();
        try {
            ZMQ.proxy(external, internal, null);
        } catch (ZError.IOException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ClosedChannelException) {
                stop();
            } else {
                throw ex;
            }
        }
    }

    public void stop() {
        System.out.println("Stopping service on " + this.port);
        external.close();
        internal.close();
        context.term();
        System.out.println("Stopped service on " + this.port);
    }

}
