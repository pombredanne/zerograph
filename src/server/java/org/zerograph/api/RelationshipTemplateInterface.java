package org.zerograph.api;

import org.neo4j.graphdb.Relationship;

import java.util.Map;

public interface RelationshipTemplateInterface {

    public String getType();

    public Map<String, Object> getProperties();

    public boolean equals(Relationship rel);

    public boolean equals(RelationshipTemplateInterface rel);

}
