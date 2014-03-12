package org.zerograph.test.helpers;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class NodeSpec {

    public static NodeSpec getAlice() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Alice");
        return new NodeSpec(Arrays.asList("Person"), properties);
    }

    public static NodeSpec getBob() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Bob");
        return new NodeSpec(Arrays.asList("Person"), properties);
    }

    public static NodeSpec getEmployee() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("employee_number", 123456);
        return new NodeSpec(Arrays.asList("Manager"), properties);
    }

    public static NodeSpec getAliceTheEmployee() {
        NodeSpec alice = getAlice();
        NodeSpec employee = getEmployee();
        ArrayList<String> labels = new ArrayList<>();
        labels.addAll(alice.getLabels());
        labels.addAll(employee.getLabels());
        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(alice.getProperties());
        properties.putAll(employee.getProperties());
        return new NodeSpec(labels, properties);
    }

    final private List<String> labels;
    final private Map<String, Object> properties;

    public NodeSpec(List<String> labels, Map<String, Object> properties) {
        this.labels = labels;
        this.properties = properties;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean matches(Node node) {
        return matchesLabels(node) && matchesProperties(node);
    }

    public boolean matchesLabels(Node node) {
        ArrayList<String> nodeLabels = new ArrayList<>();
        for (Label label : node.getLabels()) {
            nodeLabels.add(label.name());
        }
        if (nodeLabels.size() == labels.size()) {
            for (String label : labels) {
                if (!nodeLabels.contains(label)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean matchesProperties(Node node) {
        ArrayList<String> nodePropertyKeys = new ArrayList<>();
        for (String key : node.getPropertyKeys()) {
            nodePropertyKeys.add(key);
        }
        if (nodePropertyKeys.size() == properties.size()) {
            for (String key : properties.keySet()) {
                if (!nodePropertyKeys.contains(key) || !node.getProperty(key).equals(properties.get(key))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
