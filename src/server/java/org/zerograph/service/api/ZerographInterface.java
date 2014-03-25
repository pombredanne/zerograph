package org.zerograph.service.api;

import org.zerograph.except.GraphAlreadyStartedException;
import org.zerograph.except.GraphNotStartedException;
import org.zerograph.except.NoSuchGraphException;

public interface ZerographInterface extends ServiceInterface {

    public GraphInterface getGraph(String host, int port) throws NoSuchGraphException;

    public GraphInterface startGraph(String host, int port, boolean create) throws NoSuchGraphException, GraphAlreadyStartedException;

    public void stopGraph(String host, int port, boolean delete) throws GraphNotStartedException;

}
