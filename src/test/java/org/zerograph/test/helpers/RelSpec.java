package org.zerograph.test.helpers;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RelSpec {

    public static RelSpec getKnowsSince1999() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        return new RelSpec("KNOWS_SINCE_1999", properties);
    }

    public static RelSpec getKnowsFromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("from", "work");
        return new RelSpec("KNOWS_SINCE_1999", properties);
    }

    public static RelSpec getKnowsSince1999FromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        properties.put("from", "work");
        return new RelSpec("KNOWS_SINCE_1999", properties);
    }

    final private String type;
    final private Map<String, Object> properties;

    public RelSpec(String type, Map<String, Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean matches(Relationship rel) {
        return rel.getType().name().equals(type) && matchesProperties(rel);
    }

    public boolean matchesProperties(PropertyContainer entity) {
        ArrayList<String> propertyKeys = new ArrayList<>();
        for (String key : entity.getPropertyKeys()) {
            propertyKeys.add(key);
        }
        if (propertyKeys.size() == properties.size()) {
            for (String key : properties.keySet()) {
                if (!propertyKeys.contains(key) || !entity.getProperty(key).equals(properties.get(key))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
