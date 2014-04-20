package org.zerograph.yaml;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.zerograph.Graph;

import java.io.IOException;
import java.util.*;

import static org.zerograph.util.Toolbox.labelNameSet;
import static org.zerograph.util.Toolbox.propertyMap;

public class YAML {

    final private static ObjectMapper MAPPER = new ObjectMapper();
    final private static ObjectWriter STRING_WRITER = MAPPER.writerWithType(String.class);

    public static String dump(Object data) {
        if (data == null) {
            return "null";
        } else if (data instanceof Boolean) {
            return dump((Boolean) data);
        } else if (data instanceof Integer) {
            return dump((Integer) data);
        } else if (data instanceof Long) {
            return dump((Long) data);
        } else if (data instanceof Double) {
            return dump((Double) data);
        } else if (data instanceof String) {
            return dump((String) data);
        } else if (data instanceof boolean[]) {
            boolean[] arrayData = (boolean[]) data;
            ArrayList<Boolean> values = new ArrayList<>(arrayData.length);
            for (boolean value : arrayData) {
                values.add(value);
            }
            return dump(values);
        } else if (data instanceof int[]) {
            int[] arrayData = (int[]) data;
            ArrayList<Integer> values = new ArrayList<>(arrayData.length);
            for (int value : arrayData) {
                values.add(value);
            }
            return dump(values);
        } else if (data instanceof long[]) {
            long[] arrayData = (long[]) data;
            ArrayList<Long> values = new ArrayList<>(arrayData.length);
            for (long value : arrayData) {
                values.add(value);
            }
            return dump(values);
        } else if (data instanceof double[]) {
            double[] arrayData = (double[]) data;
            ArrayList<Double> values = new ArrayList<>(arrayData.length);
            for (double value : arrayData) {
                values.add(value);
            }
            return dump(values);
        } else if (data instanceof Boolean[]) {
            return dump(Arrays.asList((Boolean[]) data));
        } else if (data instanceof Integer[]) {
            return dump(Arrays.asList((Integer[]) data));
        } else if (data instanceof Long[]) {
            return dump(Arrays.asList((Long[]) data));
        } else if (data instanceof Double[]) {
            return dump(Arrays.asList((Double[]) data));
        } else if (data instanceof String[]) {
            return dump(Arrays.asList((String[]) data));
        } else if (data instanceof List) {
            return dump((List) data);
        } else if (data instanceof Set) {
            return dump((Set) data);
        } else if (data instanceof Map) {
            return dump((Map) data);
        } else if (data instanceof Node) {
            return dump((Node) data);
        } else if (data instanceof Relationship) {
            return dump((Relationship) data);
        } else if (data instanceof Path) {
            return dump((Path) data);
        } else if (data instanceof Graph) {
            return dump((Graph) data);
        } else {
            throw new IllegalArgumentException("Unyamlable type: " + data.getClass().getName());
        }
    }

    public static String dump(Boolean data) {
        if (data == null) {
            return "null";
        } else if (data) {
            return "true";
        } else {
            return "false";
        }
    }

    public static String dump(Integer data) {
        return Integer.toString(data);
    }

    public static String dump(Long data) {
        return Long.toString(data);
    }

    public static String dump(Double data) {
        return Double.toString(data);
    }

    public static String dump(String data) {
        try {
            return STRING_WRITER.writeValueAsString(data);
        } catch (IOException ex) {
            return '"' + data + '"';
        }
    }

    public static String dump(List data) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        int count = 0;
        for (Object item : data) {
            if (count > 0) {
                builder.append(',');
            }
            builder.append(dump(item));
            count += 1;
        }
        builder.append(']');
        return builder.toString();
    }

    public static String dump(Set data) {
        return dump(Arrays.asList(data.toArray()));
    }

    public static String dump(Map data) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        int count = 0;
        for (Object key : data.keySet()) {
            if (count > 0) {
                builder.append(',');
            }
            builder.append(dump(key.toString()));
            builder.append(':');
            builder.append(dump(data.get(key)));
            count += 1;
        }
        builder.append('}');
        return builder.toString();
    }

    public static String dump(Graph data) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(2);
        attributes.put("host", data.getHost());
        attributes.put("port", data.getPort());
        return "!Graph " + dump(attributes);
    }

    public static String dump(Node data) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(3);
        attributes.put("id", data.getId());
        Set<String> labels = labelNameSet(data.getLabels());
        if (labels.size() > 0) {
            attributes.put("labels", labels);
        }
        Map<String, Object> properties = propertyMap(data);
        if (properties.size() > 0) {
            attributes.put("properties", properties);
        }
        return "!Node " + dump(attributes);
    }

    public static String dump(Relationship data) {
        StringBuilder builder = new StringBuilder();
        builder.append("!Path [");
        builder.append(dump(data.getStartNode()));
        builder.append(',');
        builder.append(dumpRel(data));
        builder.append(',');
        builder.append(dump(data.getEndNode()));
        builder.append(']');
        return builder.toString();
    }

    public static String dump(Path data) {
        StringBuilder builder = new StringBuilder();
        builder.append("!Path ");
        Node node = null;
        char link = '[';
        for (PropertyContainer entity : data) {
            builder.append(link);
            if (entity instanceof Node) {
                node = (Node) entity;
                builder.append(dump(node));
            } else if (entity instanceof Relationship) {
                Relationship rel = (Relationship) entity;
                if (node != null) {
                    long nodeID = node.getId();
                    if (rel.getStartNode().getId() != nodeID && rel.getEndNode().getId() == nodeID) {
                        builder.append(dumpRev(rel));
                    } else {
                        builder.append(dumpRel(rel));
                    }
                }
            }
            link = ',';
        }
        builder.append(']');
        return builder.toString();
    }

    private static String dumpRel(Relationship data) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(3);
        attributes.put("id", data.getId());
        attributes.put("type", data.getType().name());
        Map<String, Object> properties = propertyMap(data);
        if (properties.size() > 0) {
            attributes.put("properties", properties);
        }
        return "!Rel " + dump(attributes);
    }

    private static String dumpRev(Relationship data) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(3);
        attributes.put("id", data.getId());
        attributes.put("type", data.getType().name());
        Map<String, Object> properties = propertyMap(data);
        if (properties.size() > 0) {
            attributes.put("properties", properties);
        }
        return "!Rev " + dump(attributes);
    }

}
