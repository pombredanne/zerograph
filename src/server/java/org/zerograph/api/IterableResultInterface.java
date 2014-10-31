package org.zerograph.api;

import org.zerograph.Statistics;

import java.util.Iterator;

public interface IterableResultInterface<T> extends Iterable<T> {

    public T getFirst();

    public Iterator<T> iterator();

    public Statistics getStatistics();

}
