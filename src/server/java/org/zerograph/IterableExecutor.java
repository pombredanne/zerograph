package org.zerograph;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;

import java.util.Iterator;
import java.util.Map;

public class IterableExecutor<T> {

    final private ExecutionEngine engine;

    public IterableExecutor(ExecutionEngine engine) {
        this.engine = engine;

    }

    public Iterable<T> execute(String query, Map<String, Object> params, final String column) {
        final ExecutionResult result = engine.execute(query, params);

        return new IterableResult<T>() {

            final Iterator<Map<String, Object>> resultIterator = result.iterator();

            @Override
            public Statistics getStatistics() {
                return new Statistics(result.getQueryStatistics());
            }

            @Override
            public Iterator<T> iterator() {

                return new Iterator<T>() {

                    public boolean hasNext() {
                        return resultIterator.hasNext();
                    }

                    public T next() {
                        Map<String, Object> row = resultIterator.next();
                        Object value = row.get(column);

                        @SuppressWarnings("unchecked")
                        T relationshipValue = (T) value;

                        if (getFirst() == null) {
                            setFirst(relationshipValue);
                        }
                        return relationshipValue;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };

            }

        };

    }

}
