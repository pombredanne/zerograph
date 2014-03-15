package org.zerograph.util;

import org.neo4j.graphdb.Node;
import org.zerograph.api.NodeTemplateInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.zerograph.util.Toolbox.equalMaps;
import static org.zerograph.util.Toolbox.labelNameSet;
import static org.zerograph.util.Toolbox.propertyMap;

public class NodeTemplate implements NodeTemplateInterface {

    final private HashSet<String> labels;
    final private HashMap<String, Object> properties;

    public NodeTemplate(Collection<String> labels, Map<String, Object> properties) {
        this.labels = new HashSet<>(labels);
        this.properties = new HashMap<>(properties);
    }

    @Override
    public Set<String> getLabels() {
        return labels;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Node node) {
        return labels.equals(labelNameSet(node.getLabels())) && equalMaps(properties, propertyMap(node));
    }

    @Override
    public boolean equals(NodeTemplateInterface node) {
        return labels.equals(node.getLabels()) && equalMaps(properties, node.getProperties());
    }

}
