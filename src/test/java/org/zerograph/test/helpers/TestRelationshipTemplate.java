package org.zerograph.test.helpers;

import org.zerograph.api.RelationshipTemplateInterface;
import org.zerograph.util.RelationshipTemplate;

import java.util.HashMap;
import java.util.Map;

public class TestRelationshipTemplate extends RelationshipTemplate implements RelationshipTemplateInterface {

    public static RelationshipTemplate getKnowsSince1999() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        return new TestRelationshipTemplate("KNOWS_SINCE_1999", properties);
    }

    public static RelationshipTemplate getKnowsFromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("from", "work");
        return new TestRelationshipTemplate("KNOWS_SINCE_1999", properties);
    }

    public static RelationshipTemplate getKnowsSince1999FromWork() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("since", 1999);
        properties.put("from", "work");
        return new TestRelationshipTemplate("KNOWS_SINCE_1999", properties);
    }

    public TestRelationshipTemplate(String type, Map<String, Object> properties) {
        super(type, properties);
    }

}
