package org.zerograph.test.helpers;

import org.zerograph.api.NodeTemplateInterface;
import org.zerograph.util.NodeTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestNodeTemplate extends NodeTemplate implements NodeTemplateInterface {

    public static NodeTemplate getAlice() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Alice");
        return new NodeTemplate(Arrays.asList("Person"), properties);
    }

    public static NodeTemplate getBob() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Bob");
        return new NodeTemplate(Arrays.asList("Person"), properties);
    }

    public static NodeTemplate getEmployee() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("employee_number", 123456);
        return new NodeTemplate(Arrays.asList("Manager"), properties);
    }

    public static NodeTemplate getAliceTheEmployee() {
        NodeTemplate alice = getAlice();
        NodeTemplate employee = getEmployee();
        ArrayList<String> labels = new ArrayList<>();
        labels.addAll(alice.getLabels());
        labels.addAll(employee.getLabels());
        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(alice.getProperties());
        properties.putAll(employee.getProperties());
        return new NodeTemplate(labels, properties);
    }

    public TestNodeTemplate(List<String> labels, Map<String, Object> properties) {
        super(labels, properties);
    }

}
