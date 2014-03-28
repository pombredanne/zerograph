package org.zerograph;

public class Zerograph {

    public static void main(String[] args) {
        // TODO: add shutdown hook
        Graph graph = new Graph("localhost", 47470);
        Thread thread = new Thread(graph);
        thread.start();
    }

}
