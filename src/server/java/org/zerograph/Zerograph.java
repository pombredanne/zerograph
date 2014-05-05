package org.zerograph;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.zerograph.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Zerograph implements Daemon {

    public static int ZEROGRAPH_PORT = 47470;

    public static void main(String... args) throws Exception {
        Zerograph zerograph = new Zerograph();
        zerograph.start();
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {

    }

    @Override
    public void start() throws Exception {
        Thread.currentThread().setName("-----/-------");
        Set<Graph> graphs = detectGraphs();
        Log.write("Starting graph services");
        for (Graph graph : graphs) {
            Thread thread = new Thread(graph);
            thread.setName(graph.getPort() + "/-------");
            thread.start();
        }
    }

    @Override
    public void stop() throws Exception {
        Set<Graph> graphs = Graph.getAllRunning();
        Log.write("Stopping graph services");
        for (Graph graph : graphs) {
            graph.stop();
        }
    }

    @Override
    public void destroy() {

    }

    private Set<Graph> detectGraphs() {
        Log.write("Detecting graphs");
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
        HashSet<Graph> graphs = new HashSet<>();
        for (int port : ports) {
            graphs.add(new Graph("localhost", port));
        }
        return graphs;
    }

}
