package org.zerograph.api;

import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.Set;

public interface NodeTemplateInterface {

    public Set<String> getLabels();

    public Map<String, Object> getProperties();

    public boolean equals(Node node);

    public boolean equals(NodeTemplateInterface node);

}
