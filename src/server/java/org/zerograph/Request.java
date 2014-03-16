package org.zerograph;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.RequestInterface;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.util.Data;
import org.zerograph.util.Pointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request is tab-separated string of terms
 *
 * METHOD resource [data [data ...]]
 *
 */
public class Request implements RequestInterface {

    final private static ObjectMapper mapper = new ObjectMapper();

    final private String string;
    final private String method;
    final private String resource;
    final private Object[] data;

    public Request(String string) throws Status4xx {
        this.string = string;
        String[] parts = string.split("\t");
        if (parts.length < 2) {
            throw new BadRequest(string);
        }
        this.method = parts[0];
        this.resource = parts[1];
        int dataSize = parts.length - 2;
        ArrayList<Object> data = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            try {
                data.add(Data.decode(parts[i + 2]));
            } catch (IOException ex) {
                throw new BadRequest(parts[i + 2]);
            }
        }
        this.data = data.toArray(new Object[dataSize]);
    }

    public String toString() {
        return this.string;
    }

    public String getMethod() {
        return this.method;
    }

    public String getResourceName() {
        return this.resource;
    }

    public Object getData(int index) {
        if (index >= 0 && index < this.data.length) {
            return this.data[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public Object getData(int index, Object defaultValue) {
        if (index >= 0 && index < this.data.length) {
            return this.data[index];
        } else {
            return defaultValue;
        }
    }

    public boolean getBooleanData(int index) {
        Object datum = getData(index);
        if (datum instanceof Boolean) {
            return (Boolean)datum;
        } else {
            throw new IllegalArgumentException("Boolean data expected");
        }
    }

    public boolean getBooleanData(int index, boolean defaultValue) {
        Object datum = getData(index, defaultValue);
        if (datum instanceof Boolean) {
            return (Boolean)datum;
        } else {
            throw new IllegalArgumentException("Boolean data expected");
        }
    }

    public long getLongData(int index) {
        Object datum = getData(index);
        if (datum instanceof Integer) {
            return ((Integer) datum).longValue();
        } else if (datum instanceof Long) {
            return (Long) datum;
        } else {
            throw new IllegalArgumentException("Integer or Long data expected");
        }
    }

    public long getLongData(int index, long defaultValue) {
        Object datum = getData(index, defaultValue);
        if (datum instanceof Integer) {
            return ((Integer) datum).longValue();
        } else if (datum instanceof Long) {
            return (Long) datum;
        } else {
            throw new IllegalArgumentException("Integer or Long data expected");
        }
    }

    public String getStringData(int index) {
        Object datum = getData(index);
        if (datum instanceof String) {
            return (String)datum;
        } else {
            throw new IllegalArgumentException("String data expected");
        }
    }

    public String getStringData(int index, String defaultValue) {
        Object datum = getData(index, defaultValue);
        if (datum instanceof String) {
            return (String)datum;
        } else {
            throw new IllegalArgumentException("String data expected");
        }
    }

    public List getListData(int index) {
        Object datum = getData(index);
        if (datum instanceof List) {
            return (List)datum;
        } else {
            throw new IllegalArgumentException("List data expected");
        }
    }

    public List getListData(int index, List defaultValue) {
        Object datum = getData(index, defaultValue);
        if (datum instanceof List) {
            return (List)datum;
        } else {
            throw new IllegalArgumentException("List data expected");
        }
    }

    public Map getMapData(int index) {
        Object datum = getData(index);
        if (datum instanceof Map) {
            return (Map)datum;
        } else {
            throw new IllegalArgumentException("Map data expected");
        }
    }

    public Map getMapData(int index, Map defaultValue) {
        Object datum = getData(index, defaultValue);
        if (datum instanceof Map) {
            return (Map)datum;
        } else {
            throw new IllegalArgumentException("Map data expected");
        }
    }

    public void resolvePointers(List<PropertyContainer> values) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Pointer) {
                Pointer pointer = (Pointer)data[i];
                data[i] = values.get(pointer.getAddress());
            }
        }
    }

}
