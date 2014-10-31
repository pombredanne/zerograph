package org.zerograph.test.helpers;

import java.util.HashMap;
import java.util.Map;

public class QuickMap {

    public static Map<String, Object> from(Object... keysAndValues) {
        return new QuickMap(keysAndValues).toMap();
    }

    final private HashMap<String, Object> map;

    public QuickMap(Object... keysAndValues) {
        if (keysAndValues.length % 2 == 0) {
            this.map = new HashMap<>(keysAndValues.length / 2);
        } else {
            throw new IllegalArgumentException("Keys and values must come in pairs");
        }
        String key = null;
        for (Object value : keysAndValues) {
            if (key == null) {
                key = value.toString();
            } else {
                map.put(key, value);
                key = null;
            }
        }
    }

    public Map<String, Object> toMap() {
        return map;
    }

}
