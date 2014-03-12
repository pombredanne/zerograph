package org.zerograph.api;

import org.neo4j.graphdb.PropertyContainer;

import java.util.List;
import java.util.Map;

public interface RequestInterface {

    public String getMethod();

    public String getResource();

    public Object getData(int index);

    public Object getData(int index, Object defaultValue);

    public boolean getBooleanData(int index);

    public boolean getBooleanData(int index, boolean defaultValue);

    public long getLongData(int index);

    public long getLongData(int index, long defaultValue);

    public String getStringData(int index);

    public String getStringData(int index, String defaultValue);

    public List getListData(int index);

    public List getListData(int index, List defaultValue);

    public Map getMapData(int index);

    public Map getMapData(int index, Map defaultValue);

    public void resolvePointers(List<PropertyContainer> values);

}
