package org.zerograph;

import java.io.File;
import java.util.HashSet;

public class Zerograph {

    public static int ZEROGRAPH_PORT = 47470;

    final private HashSet<Graph> graphs;

    public Zerograph() {
        System.out.println("Starting Zerograph");
        this.graphs = new HashSet<>();
    }

    public synchronized void detectGraphs() {
        System.out.println("Detecting graphs");
        HashSet<Integer> ports = new HashSet<>();
        ports.add(ZEROGRAPH_PORT);
        for (File directory : GraphDirectory.getAllExisting()) {
            try {
                int port = Integer.parseInt(directory.getName());
                ports.add(port);
            } catch (ClassCastException ex) {
                // shouldn't happen :-/
            }
        }
        graphs.clear();
        for (int port : ports) {
            graphs.add(new Graph("localhost", port));
        }
    }

    public synchronized void start() {
        for (Graph graph : graphs) {
            Thread thread = new Thread(graph);
            thread.start();
        }
    }

    public static void main(String[] args) {
        // TODO: add shutdown hook
        Zerograph zerograph = new Zerograph();
        zerograph.detectGraphs();
        zerograph.start();
    }

}
