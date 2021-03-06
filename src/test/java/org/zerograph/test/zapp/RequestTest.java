package org.zerograph.test.zapp;

import org.junit.Test;
import org.zerograph.except.ClientError;
import org.zerograph.except.MalformedRequest;
import org.zerograph.Request;

import java.util.HashMap;

public class RequestTest {

    @Test
    public void testCanParseRequestStringWithoutData() throws MalformedRequest {
        String string = "GET Node";
        Request request = Request.parse(string);
        assert request.getMethod().equals("GET");
        assert request.getResource().equals("Node");
    }

    @Test
    public void testCanConvertRequestWithoutDataToString() {
        Request request = new Request("GET", "Node");
        assert request.getMethod().equals("GET");
        assert request.getResource().equals("Node");
        String string = request.toString();
        assert string.equals("GET Node");
    }

    @Test
    public void testCanParseRequestStringWithData() throws ClientError {
        String string = "GET Node {\"id\":1}";
        Request request = Request.parse(string);
        assert request.getMethod().equals("GET");
        assert request.getResource().equals("Node");
        assert request.getArgument("id") == 1;
    }

    @Test
    public void testCanConvertRequestWithDataToString() throws ClientError {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("id", 1);
        Request request = new Request("GET", "Node", properties);
        assert request.getMethod().equals("GET");
        assert request.getResource().equals("Node");
        assert request.getArgument("id") == 1;
        String string = request.toString();
        assert string.equals("GET Node {\"id\":1}");
    }

}
