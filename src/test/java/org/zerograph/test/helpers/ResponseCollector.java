package org.zerograph.test.helpers;

import org.zerograph.api.ResponderInterface;
import org.zerograph.api.ResponseInterface;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector implements ResponderInterface {

    final private ArrayList<ResponseInterface> responses;

    public ResponseCollector() {
        responses = new ArrayList<>();
    }

    @Override
    public void respond(ResponseInterface response) {
        responses.add(response);
    }

    public int getResponseCount() {
        return this.responses.size();
    }

    public List<ResponseInterface> getResponses() {
        return this.responses;
    }

    public boolean matchResponse(int index, int status, Object... data) {
        ResponseInterface response = responses.get(index);
        if (response.getStatus() != status)
            return false;
        int i = 0;
        for (Object datum : response.getData()) {
            if (!datum.equals(data[i]))
                return false;
            i += 1;
        }
        return true;
    }

    public boolean matchSingleResponse(int status, Object... data) {
        return responses.size() == 1 && matchResponse(0, status, data);
    }

}
