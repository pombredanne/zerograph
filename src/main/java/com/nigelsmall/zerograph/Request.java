package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.util.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request is tab-separated string of terms
 *
 * VERB resource [data [data ...]]
 *
 */
public class Request {

    final private String string;
    final private String method;
    final private String resource;
    final private String[] data;

    public Request(String string) throws BadRequest {
        this.string = string;
        String[] parts = string.split("\t");
        if (parts.length < 2) {
            throw new BadRequest(string);
        }
        this.method = parts[0];
        this.resource = parts[1];
        int dataSize = parts.length - 2;
        ArrayList<String> data = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            data.add(parts[i + 2]);
        }
        this.data = data.toArray(new String[dataSize]);
    }

    public String toString() {
        return this.string;
    }

    public String getMethod() {
        return this.method;
    }

    public String getResource() {
        return this.resource;
    }

    public String getStringData(int index) throws IOException {
        return JSON.decodeString(data[index]);
    }

    public int getIntegerData(int index) throws IOException {
        return JSON.decodeInteger(data[index]);
    }

    public List getListData(int index) throws IOException {
        return JSON.decodeList(data[index]);
    }

    public Map getMapData(int index) throws IOException {
        return JSON.decodeMap(data[index]);
    }

}
