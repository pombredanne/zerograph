package org.zerograph.util;

import org.neo4j.graphdb.Relationship;
import org.zerograph.api.RelationshipTemplateInterface;

import java.util.HashMap;
import java.util.Map;

import static org.zerograph.util.Toolbox.equalMaps;
import static org.zerograph.util.Toolbox.propertyMap;

public class RelationshipTemplate implements RelationshipTemplateInterface {

    final private String type;
    final private HashMap<String, Object> properties;

    public RelationshipTemplate(String type, Map<String, Object> properties) {
        this.type = type;
        this.properties = new HashMap<>(properties);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Relationship rel) {
        return type.equals(rel.getType().name()) && equalMaps(properties, propertyMap(rel));
    }

    @Override
    public boolean equals(RelationshipTemplateInterface rel) {
        return type.equals(rel.getType()) && equalMaps(properties, rel.getProperties());
    }

}
