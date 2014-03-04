package org.zerograph;

import org.zeromq.ZMQ;

import java.util.HashMap;

public class Service implements Runnable {

    final static private HashMap<Integer, Thread> instances = new HashMap<>(1);

    final public static int WORKER_COUNT = 40;

    final private Environment env;
    final private int port;
    final private String address;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(int port) {
        this.env = Environment.getInstance();
        this.port = port;
        this.address = "tcp://*:" + port;
    }

    public synchronized static void start(int port) {
        if (instances.containsKey(port)) {
            // TODO: already running
        } else {
            Service service = new Service(port);
            Thread thread = new Thread(service);
            thread.start();
            instances.put(port, thread);
        }
    }

    public synchronized static void stop(int port) {
        if (instances.containsKey(port)) {
            Thread thread = instances.get(port);
            // TODO: can't kill current db
            thread.interrupt();
        } else {
            // TODO: not running
        }
    }

    private void bind() {
        this.external = env.getContext().socket(ZMQ.ROUTER);
        this.external.bind(address);
        this.internal = env.getContext().socket(ZMQ.DEALER);
        this.internal.bind(Worker.ADDRESS);
    }

    private void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new Worker(env, port)).start();
        }
    }

    public void run() {
        System.out.println("Starting up " + this.port);
        bind();
        startWorkers(WORKER_COUNT);
        ZMQ.proxy(external, internal, null);
    }

    public void shutdown() {
        System.out.println("Shutting down " + this.port);
        this.external.close();
        this.internal.close();
        this.env.getContext().term();
    }

}
