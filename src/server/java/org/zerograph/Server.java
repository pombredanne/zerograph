package org.zerograph;

import org.zeromq.ZMQ;

import java.io.IOException;

/**
 * Experimental Neo4j Server using ZeroMQ
 *
 */
public class Server {

    final public static int WORKER_COUNT = 40;

    final private Environment env;
    final private int port;
    final private String address;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Server(Environment env, int port) {
        this.env = env;
        this.port = port;
        this.address = "tcp://*:" + port;
    }

    public void start() throws InterruptedException, IOException {
        // bind sockets
        this.external = env.getContext().socket(ZMQ.ROUTER);
        this.external.bind(address);
        this.internal = env.getContext().socket(ZMQ.DEALER);
        this.internal.bind(Worker.ADDRESS);
        // start worker threads
        for(int i = 0; i < WORKER_COUNT; i++) {
            new Thread(new Worker(env, port)).start();
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
        String storagePath = getStoragePath();
        Environment env = new Environment(storagePath);
        Server server = new Server(env, 47474);
        server.start();
    }

    public static String getStoragePath() {
        String storagePath = System.getenv("ZG_STORAGE_PATH");
        if (storagePath != null)
            return storagePath;
        String userName = System.getProperty("user.name");
        if ("root".equals(userName))
            return "/var/zerograph";
        else
            return System.getProperty("user.home") + "/" + ".zerograph";
    }

}
