package org.zerograph.zpp;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.MalformedRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Request implements RequestInterface {

    final private static TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<Map<String, Object>>() {};
    final private static ObjectMapper MAPPER = new ObjectMapper();
    final private static ObjectWriter WRITER = MAPPER.writerWithType(JSON_OBJECT);

    public static Request parse(String string) throws MalformedRequest {
        String[] parts = string.split(" ", 3);
        if (parts.length < 2) {
            throw new MalformedRequest(string);
        }
        if (parts.length == 2) {
            return new Request(parts[0], parts[1]);
        } else {
            try {
                Map<String, Object> data = MAPPER.readValue(parts[2], JSON_OBJECT);
                return new Request(parts[0], parts[1], data);
            } catch (IOException e) {
                throw new MalformedRequest(string);
            }
        }
    }

    final private String method;
    final private String resource;
    final private Map<String, Object> arguments;

    public Request(String method, String resource, Map<String, Object> arguments) {
        this.method = method;
        this.resource = resource;
        this.arguments = arguments;
    }

    public Request(String method, String resource) {
        this(method, resource, null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method);
        builder.append(' ');
        builder.append(resource);
        if (arguments != null) {
            builder.append(' ');
            try {
                builder.append(WRITER.writeValueAsString(arguments));
            } catch (IOException ex) {
                throw new RuntimeException("Unable to serialise request", ex);
            }
        }
        return builder.toString();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public Object getArgument(String name) throws ClientError {
        if (arguments == null) {
            throw new ClientError("Missing argument: " + name);
        } else if (arguments.containsKey(name)) {
            return arguments.get(name);
        } else {
            throw new ClientError("Missing argument: " + name);
        }
    }

    @Override
    public Boolean getArgumentAsBoolean(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Boolean) {
                return ((Boolean) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Boolean");
            }
        } else {
            throw new ClientError("Missing Boolean argument: " + name);
        }
    }

    @Override
    public Integer getArgumentAsInteger(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Integer");
            }
        } else {
            throw new ClientError("Missing Integer argument: " + name);
        }
    }

    @Override
    public Long getArgumentAsLong(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Long");
            }
        } else {
            throw new ClientError("Missing Long argument: " + name);
        }
    }

    @Override
    public Double getArgumentAsDouble(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Double");
            }
        } else {
            throw new ClientError("Missing Double argument: " + name);
        }
    }

    @Override
    public String getArgumentAsString(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else {
                return value.toString();
            }
        } else {
            throw new ClientError("Missing String argument: " + name);
        }
    }

    @Override
    public List getArgumentAsList(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof List) {
                return ((List) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to List");
            }
        } else {
            throw new ClientError("Missing List argument: " + name);
        }
    }

    @Override
    public Map getArgumentAsMap(String name) throws ClientError {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                return ((Map) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Map");
            }
        } else {
            throw new ClientError("Missing Map argument: " + name);
        }
    }

    @Override
    public Object getArgument(String name, Object defaultValue) {
        if (arguments == null) {
            return defaultValue;
        } else if (arguments.containsKey(name)) {
            return arguments.get(name);
        } else {
            return defaultValue;
        }
    }

    @Override
    public Boolean getArgumentAsBoolean(String name, Boolean defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Boolean) {
                return ((Boolean) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Boolean");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Integer getArgumentAsInteger(String name, Integer defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Integer");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Long getArgumentAsLong(String name, Long defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Long");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Double getArgumentAsDouble(String name, Double defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Double");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public String getArgumentAsString(String name, String defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else {
                return value.toString();
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public List getArgumentAsList(String name, List defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof List) {
                return ((List) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to List");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Map getArgumentAsMap(String name, Map defaultValue) {
        if (arguments.containsKey(name)) {
            Object value = arguments.get(name);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                return ((Map) value);
            } else {
                throw new ClassCastException("Cannot cast argument \"" + name + "\" to Map");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public void resolvePointers(List<PropertyContainer> values) {
        // TODO
    }
}
