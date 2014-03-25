package org.zerograph.service;

import org.zerograph.Environment;
import org.zerograph.ResourceSet;
import org.zerograph.service.api.GraphInterface;
import org.zerograph.zap.GraphMapResource;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.service.api.ZerographInterface;
import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;

/**
 * The Zerograph is the root control service for the entire server. It is
 * not backed by a database itself but provides facilities to start, stop,
 * create and delete Graph services on other ports.
 *
 */
public class Zerograph extends Service implements ZerographInterface {

    final private Environment environment;

    public Zerograph(Environment environment) {
        super(null, environment.getHost(), environment.getPort());
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public Zerograph getZerograph() {
        return this;  // a Zerograph is its own Zerograph
    }

    @Override
    public GraphInterface getGraph(String host, int port) {
        return Graph.getInstance(this, host, port);
    }

    @Override
    public GraphInterface startGraph(String host, int port, boolean create) throws NoSuchGraphException, GraphAlreadyStartedException {
        return Graph.startInstance(this, host, port, create);
    }

    @Override
    public void stopGraph(String host, int port, boolean delete) throws GraphNotStartedException {
        Graph.stopInstance(this, host, port, delete);
    }

    public void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new ZerographWorker(this)).start();
        }
    }

    @Override
    public ResourceSet createResourceSet(ResponderInterface responder) {
        ResourceSet resourceSet = new ResourceSet();
        resourceSet.add(new GraphMapResource(this, responder));
        return resourceSet;
    }

    public static void main(String[] args) {
        // TODO: add shutdown hook
        Environment env = new Environment();
        Zerograph zerograph = new Zerograph(env);
        Thread thread = new Thread(zerograph);
        thread.start();
    }

}
