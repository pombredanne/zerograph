package org.zerograph.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.Graph;
import org.zerograph.GraphDirectory;
import org.zerograph.Zerograph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

    final private static String ZEROGRAPH_HINT = "/*Zerograph*/";
    final private static String GRAPH_HINT = "/*Graph*/";
    final private static String NODE_HINT = "/*Node*/";
    final private static String REL_HINT = "/*Rel*/";
    final private static String POINTER_HINT = "/*Pointer*/";

    final private static ObjectMapper mapper = new ObjectMapper();

    private static Object decodePointer(String string) throws IOException {
        int address = mapper.readValue(string, Integer.class);
        return new Pointer(address);
    }

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

    private static Map<String, Object> attributes(Zerograph zerograph) throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("host", zerograph.getHost());
        attributes.put("port", zerograph.getPort());
        return attributes;
    }

    private static Map<String, Object> attributes(Graph graph) throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("zerograph", attributes(graph.getZerograph()));
        attributes.put("host", graph.getHost());
        attributes.put("port", graph.getPort());
        return attributes;
    }

    private static Map<String, Object> attributes(GraphDirectory directory) throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("zerograph", attributes(directory.getZerograph()));
        attributes.put("host", directory.getHost());
        attributes.put("port", directory.getPort());
        return attributes;
    }

    private static Map<String, Object> attributes(Node node) throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("id", node.getId());
        attributes.put("labels", labels(node));
        attributes.put("properties", properties(node));
        return attributes;
    }

    private static Map<String, Object> attributes(Relationship rel) throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("id", rel.getId());
        attributes.put("start", attributes(rel.getStartNode()));
        attributes.put("end", attributes(rel.getEndNode()));
        attributes.put("type", rel.getType().name());
        attributes.put("properties", properties(rel));
        return attributes;
    }

    public static String encode(Object value) throws IOException {
        if (value instanceof Zerograph) {
            return ZEROGRAPH_HINT + mapper.writeValueAsString(attributes((Zerograph) value));
        } else if (value instanceof Graph) {
            return GRAPH_HINT + mapper.writeValueAsString(attributes((Graph) value));
        } else if (value instanceof GraphDirectory) {
            return GRAPH_HINT + mapper.writeValueAsString(attributes((GraphDirectory) value));
        } else if (value instanceof Node) {
            return NODE_HINT + mapper.writeValueAsString(attributes((Node) value));
        } else if(value instanceof Relationship) {
            return REL_HINT + mapper.writeValueAsString(attributes((Relationship) value));
        } else {
            return mapper.writeValueAsString(value);
        }
    }

    public static Object decode(String string) throws IOException {
        switch (string) {
            case "null":
                return null;
            case "true":
                return true;
            case "false":
                return false;
            default:
                if (string.length() > 0) {
                    char ch = string.charAt(0);
                    if ((ch >= '0' && ch <= '9') || ch == '-') {
                        return mapper.readValue(string, Number.class);
                    } else if (ch == '"') {
                        return mapper.readValue(string, String.class);
                    } else if (ch == '[') {
                        return mapper.readValue(string, List.class);
                    } else if (ch == '{') {
                        return mapper.readValue(string, Map.class);
                    } else if (ch == '/') {
                        if (string.startsWith(POINTER_HINT)) {
                            return decodePointer(string.substring(POINTER_HINT.length()));
                        } else {
                            throw new IllegalArgumentException(string);
                        }
                    } else {
                        throw new IllegalArgumentException(string);
                    }
                } else {
                    throw new IllegalArgumentException(string);
                }
        }
    }

}
