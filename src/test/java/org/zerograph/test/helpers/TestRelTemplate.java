package org.zerograph.test.helpers;

import org.zerograph.api.RelTemplateInterface;
import org.zerograph.util.RelTemplate;

import java.util.HashMap;
import java.util.Map;

public class TestRelTemplate extends RelTemplate implements RelTemplateInterface {

    public static RelTemplate getKnowsSince1999() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        return new TestRelTemplate("KNOWS_SINCE_1999", properties);
    }

    public static RelTemplate getKnowsFromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("from", "work");
        return new TestRelTemplate("KNOWS_SINCE_1999", properties);
    }

    public static RelTemplate getKnowsSince1999FromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        properties.put("from", "work");
        return new TestRelTemplate("KNOWS_SINCE_1999", properties);
    }

    public TestRelTemplate(String type, Map<String, Object> properties) {
        super(type, properties);
    }

}
