package org.zerograph;

import org.zerograph.worker.ZerographWorker;

public class Zerograph extends Service {

    public Zerograph(String host, int port) {
        super(host, port);
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new ZerographWorker(this)).start();
        }
    }

    public static void main(String[] args) {
        // TODO: add shutdown hook
        Zerograph zerograph = new Zerograph("localhost", 47470);
        Thread thread = new Thread(zerograph);
        thread.start();
    }

}
