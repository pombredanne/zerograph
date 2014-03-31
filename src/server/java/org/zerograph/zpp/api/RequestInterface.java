package org.zerograph.zpp.api;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.zpp.except.ClientError;

import java.util.List;
import java.util.Map;

public interface RequestInterface {

    public String getMethod();

    public String getResource();

    public Object getArgument(String name) throws ClientError;

    public Boolean getArgumentAsBoolean(String name) throws ClientError;

    public Integer getArgumentAsInteger(String name) throws ClientError;

    public Long getArgumentAsLong(String name) throws ClientError;

    public Double getArgumentAsDouble(String name) throws ClientError;

    public String getArgumentAsString(String name) throws ClientError;

    public List getArgumentAsList(String name) throws ClientError;

    public Map<String, Object> getArgumentAsMap(String name) throws ClientError;

    public Object getArgument(String name, Object defaultValue);

    public Boolean getArgumentAsBoolean(String name, Boolean defaultValue);

    public Integer getArgumentAsInteger(String name, Integer defaultValue);

    public Long getArgumentAsLong(String name, Long defaultValue);

    public Double getArgumentAsDouble(String name, Double defaultValue);

    public String getArgumentAsString(String name, String defaultValue);

    public List getArgumentAsList(String name, List defaultValue);

    public Map<String, Object> getArgumentAsMap(String name, Map<String, Object> defaultValue);

    public void resolvePointers(List<PropertyContainer> values);

}
