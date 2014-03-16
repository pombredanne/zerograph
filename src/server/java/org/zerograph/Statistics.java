package org.zerograph;

import org.neo4j.cypher.javacompat.QueryStatistics;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

    final private HashMap<String, Number> statistics;

    public Statistics(QueryStatistics queryStatistics) {
        this.statistics = new HashMap<>();
        this.statistics.put("nodes_created", queryStatistics.getNodesCreated());
    }

    public Map<String, Number> toMap() {
        return statistics;
    }

    public Number get(String key) {
        return statistics.get(key);
    }

}
