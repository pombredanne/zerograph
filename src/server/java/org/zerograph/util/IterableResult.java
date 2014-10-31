package org.zerograph.util;

import org.zerograph.Statistics;
import org.zerograph.api.IterableResultInterface;

import java.util.Iterator;

public class IterableResult<T> implements IterableResultInterface<T> {

    final private Iterable<T> iterable;

    private T first;

    public IterableResult(Iterable<T> iterable) {
        this.iterable = iterable;
        this.first = null;
    }

    public IterableResult() {
        this(null);
    }

    @Override
    public T getFirst() {
        return first;
    }

    protected void setFirst(T value) {
        this.first = value;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            final private Iterator<T> iterator = iterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                T value = iterator.next();
                if (first == null)
                    first = value;
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

}
