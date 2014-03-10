package org.zerograph.test;

import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.RequestInterface;

import java.util.List;
import java.util.Map;

public class FakeRequest implements RequestInterface {

    final private String method;
    final private String resource;
    final private Object[] data;

    public FakeRequest(String method, String resource, Object... data) {
        this.method = method;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getResource() {
        return this.resource;
    }

    @Override
    public Object getData(int index) {
        return data[index];
    }

    @Override
    public Object getData(int index, Object defaultValue) {
        if (index < data.length)
            return data[index];
        else
            return defaultValue;
    }

    @Override
    public boolean getBooleanData(int index) {
        return (boolean)data[index];
    }

    @Override
    public boolean getBooleanData(int index, boolean defaultValue) {
        if (index < data.length)
            return (boolean)data[index];
        else
            return defaultValue;
    }

    @Override
    public int getIntegerData(int index) {
        return (int)data[index];
    }

    @Override
    public int getIntegerData(int index, int defaultValue) {
        if (index < data.length)
            return (int)data[index];
        else
            return defaultValue;
    }

    @Override
    public String getStringData(int index) {
        return (String)data[index];
    }

    @Override
    public String getStringData(int index, String defaultValue) {
        if (index < data.length)
            return (String)data[index];
        else
            return defaultValue;
    }

    @Override
    public List getListData(int index) {
        return (List)data[index];
    }

    @Override
    public List getListData(int index, List defaultValue) {
        if (index < data.length)
            return (List)data[index];
        else
            return defaultValue;
    }

    @Override
    public Map getMapData(int index) {
        return (Map)data[index];
    }

    @Override
    public Map getMapData(int index, Map defaultValue) {
        if (index < data.length)
            return (Map)data[index];
        else
            return defaultValue;
    }

    @Override
    public void resolvePointers(List<PropertyContainer> values) {

    }

}
