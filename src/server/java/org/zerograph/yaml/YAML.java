package org.zerograph.yaml;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.neo4j.graphdb.Node;
import org.zerograph.service.Graph;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zerograph.util.Toolbox.labelNameSet;
import static org.zerograph.util.Toolbox.propertyMap;

public class YAML {

    final private static ObjectMapper MAPPER = new ObjectMapper();
    final private static ObjectWriter STRING_WRITER = MAPPER.writerWithType(String.class);

    public static String dump(Object data) {
        if (data instanceof Boolean) {
            return dump((Boolean) data);
        } else if (data instanceof Integer) {
            return dump((Integer) data);
        } else if (data instanceof Long) {
            return dump((Long) data);
        } else if (data instanceof Double) {
            return dump((Double) data);
        } else if (data instanceof String) {
            return dump((String) data);
        } else if (data instanceof List) {
            return dump((List) data);
        } else if (data instanceof Set) {
            return dump((Set) data);
        } else if (data instanceof Map) {
            return dump((Map) data);
        } else if (data instanceof Node) {
            return dump((Node) data);
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
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(3);
        attributes.put("host", data.getHost());
        attributes.put("port", data.getPort());
        return "!Graph " + dump(attributes);
    }

    public static String dump(Node data) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(3);
        attributes.put("id", data.getId());
        attributes.put("labels", labelNameSet(data.getLabels()));
        attributes.put("properties", propertyMap(data));
        return "!Node " + dump(attributes);
    }

}
