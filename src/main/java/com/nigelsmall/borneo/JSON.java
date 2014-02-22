package com.nigelsmall.borneo;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSON {

    final private static ObjectMapper mapper = new ObjectMapper();

    private static List<String> labels(Node node) {
        ArrayList<String> labelList = new ArrayList<>();
        for (Label label : node.getLabels()) {
            labelList.add(label.name());
        }
        return labelList;
    }

    private static Map<String, Object> properties(PropertyContainer entity) {
        HashMap<String, Object> propertyMap = new HashMap<>();
        for (String key : entity.getPropertyKeys()) {
            propertyMap.put(key, entity.getProperty(key));
        }
        return propertyMap;
    }

    public static String encode(Object value) throws IOException {
        if (value instanceof Node) {
            Node node = (Node)value;
            HashMap<String, Object> attributes = new HashMap<>();
            attributes.put("id", node.getId());
            attributes.put("labels", labels(node));
            attributes.put("properties", properties(node));
            return "Node(" + mapper.writeValueAsString(attributes) + ")";
        } else {
            return mapper.writeValueAsString(value);
        }
    }

    public static Object decode(String value) throws IOException {
        return mapper.readValue(value, Object.class);
    }

}
