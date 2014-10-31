package org.zerograph.api;

import org.neo4j.graphdb.Relationship;

import java.util.Map;

public interface RelTemplateInterface {

    public String getType();

    public Map<String, Object> getProperties();

    public boolean equals(Relationship rel);

    public boolean equals(RelTemplateInterface rel);

}
