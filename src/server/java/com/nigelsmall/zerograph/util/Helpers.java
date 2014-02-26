package com.nigelsmall.zerograph.util;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import java.util.List;
import java.util.Map;

public class Helpers {

    public static void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            node.addLabel(DynamicLabel.label(labelName.toString()));
        }
    }

    public static void removeLabels(Node node) {
        for (Label label : node.getLabels()) {
            node.removeLabel(label);
        }
    }

    public static void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    public static void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
