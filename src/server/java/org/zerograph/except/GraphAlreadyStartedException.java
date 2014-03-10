package org.zerograph.except;

import org.zerograph.Graph;
import org.zerograph.api.ZerographInterface;

public class GraphAlreadyStartedException extends GraphException {

    final private Graph graph;

    public GraphAlreadyStartedException(ZerographInterface zerograph, String host, int port, Graph graph) {
        super(zerograph, host, port);
        this.graph = graph;
    }

    public Graph getGraph() {
        return this.graph;
    }

}