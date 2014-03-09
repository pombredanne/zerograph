package org.zerograph;

import org.zerograph.worker.ZerographWorker;

/**
 * The Zerograph is the root control service for the entire server. It is
 * not backed by a database itself but provides facilities to start, stop,
 * create and delete Graph services on other ports.
 *
 */
public class Zerograph extends Service {

    final private Environment environment;

    public Zerograph(Environment environment) {
        super(null, environment.getHost(), environment.getPort());
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public Zerograph getZerograph() {
        // a Zerograph is its own Zerograph
        return this;
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new ZerographWorker(this)).start();
        }
    }

    public static void main(String[] args) {
        // TODO: add shutdown hook
        Environment env = new Environment();
        Zerograph zerograph = new Zerograph(env);
        Thread thread = new Thread(zerograph);
        thread.start();
    }

}
