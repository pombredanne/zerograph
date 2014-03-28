package org.zerograph.except;

import org.zerograph.Graph;

public class GraphAlreadyStartedException extends GraphException {

    final private Graph graph;

    public GraphAlreadyStartedException(String host, int port, Graph graph) {
        super(host, port);
        this.graph = graph;
    }

    public Graph getGraph() {
        return this.graph;
    }

}
