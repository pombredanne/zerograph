package org.zerograph.util;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.PropertyContainer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Toolbox {

    public static boolean equalMaps(Map<String, Object> first, Map<String, Object> second) {
        if (first.size() == second.size()) {
            if (first.keySet().equals(second.keySet())) {
                for (String key : first.keySet()) {
                    if (!first.get(key).equals(second.get(key)))
                        return false;
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static Set<String> labelNameSet(Iterable<Label> labels) {
        HashSet<String> labelNameSet = new HashSet<>();
        for (Label label : labels) {
            labelNameSet.add(label.name());
        }
        return labelNameSet;
    }

    public static Map<String, Object> propertyMap(PropertyContainer entity) {
        HashMap<String, Object> propertyMap = new HashMap<>();
        for (String key : entity.getPropertyKeys()) {
            propertyMap.put(key, entity.getProperty(key));
        }
        return propertyMap;
    }

    public static boolean delete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files)
                delete(f);
        }
        return file.delete();
    }

}
