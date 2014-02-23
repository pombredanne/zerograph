package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.util.JSON;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Request is tab-separated string of terms
 *
 * VERB resource [data [data ...]]
 *
 */
public class Request {

    final private String string;
    final private String verb;
    final private String resource;
    final private Object[] data;

    public Request(String string) throws BadRequest {
        this.string = string;
        String[] parts = string.split("\t");
        if (parts.length < 2) {
            throw new BadRequest(string);
        }
        this.verb = parts[0];
        this.resource = parts[1];
        int dataSize = parts.length - 2;
        ArrayList<Object> data = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            try {
                data.add(JSON.decode(parts[i + 2]));
            } catch (IOException ex) {
                throw new BadRequest(string);
            }
        }
        this.data = data.toArray(new Object[dataSize]);
    }

    public String toString() {
        return this.string;
    }

    public String getVerb() {
        return this.verb;
    }

    public String getResource() {
        return this.resource;
    }

    public Object[] getData() {
        return this.data;
    }

}
