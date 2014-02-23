package com.nigelsmall.zerograph;

import org.zeromq.ZMQ;

import java.io.IOException;

/**
 * Experimental Neo4j Server using ZeroMQ
 *
 */
public class Service {

    final public static String ADDRESS = "tcp://*:47474";
    final public static String STORAGE_DIR = "/tmp/zerograph";
    final public static int WORKER_COUNT = 40;

    private Environment env;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(Environment env) {
        this.env = env;
    }

    public void start(String address) throws InterruptedException, IOException {
        // bind sockets
        this.external = env.getContext().socket(ZMQ.ROUTER);
        this.external.bind(address);
        this.internal = env.getContext().socket(ZMQ.DEALER);
        this.internal.bind(Worker.ADDRESS);
        // start worker threads
        for(int i = 0; i < WORKER_COUNT; i++) {
            new Thread(new Worker(env)).start();
        }
        // pass through
        ZMQ.proxy(external, internal, null);
    }

    public void stop() {
        this.external.close();
        this.internal.close();
        this.env.getContext().term();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Environment env = new Environment(STORAGE_DIR);
        Service service = new Service(env);
        service.start(ADDRESS);
    }

}
