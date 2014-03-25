package org.zerograph.test.yaml;

import org.junit.Test;
import org.zerograph.test.helpers.FakeNode;
import org.zerograph.yaml.YAML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class YAMLTest {

    @Test
    public void testCanDumpBooleanTrue() throws IOException {
        String dumped = YAML.dump(true);
        assert dumped.equals("true");
    }

    @Test
    public void testCanDumpBooleanFalse() throws IOException {
        String dumped = YAML.dump(false);
        assert dumped.equals("false");
    }

    @Test
    public void testCanDumpInteger() throws IOException {
        String dumped = YAML.dump(42);
        assert dumped.equals("42");
    }

    @Test
    public void testCanDumpString() throws IOException {
        String dumped = YAML.dump("hello, world");
        assert dumped.equals("\"hello, world\"");
    }

    @Test
    public void testCanDumpList() throws IOException {
        ArrayList<Integer> value = new ArrayList<>();
        value.add(2);
        value.add(3);
        value.add(5);
        value.add(8);
        String dumped = YAML.dump(value);
        assert dumped.equals("[2,3,5,8]");
    }

    @Test
    public void testCanDumpMap() throws IOException {
        LinkedHashMap<String, String> value = new LinkedHashMap<>();
        value.put("one", "eins");
        value.put("two", "zwei");
        String dumped = YAML.dump(value);
        assert dumped.equals("{\"one\":\"eins\",\"two\":\"zwei\"}");
    }

    @Test
    public void testCanDumpNestedValues() throws IOException {
        LinkedHashMap<String, List<String>> value = new LinkedHashMap<>();
        value.put("one", Arrays.asList("eins", "un"));
        value.put("two", Arrays.asList("zwei", "deux"));
        String dumped = YAML.dump(value);
        assert dumped.equals("{\"one\":[\"eins\",\"un\"],\"two\":[\"zwei\",\"deux\"]}");
    }

    @Test
    public void testCanDumpNode() throws IOException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", "orange");
        FakeNode value = new FakeNode(42, Arrays.asList("Fruit"), properties);
        String dumped = YAML.dump(value);
        assert dumped.equals("!Node {\"id\":42,\"labels\":[\"Fruit\"],\"properties\":{\"name\":\"orange\"}}");
    }

}
