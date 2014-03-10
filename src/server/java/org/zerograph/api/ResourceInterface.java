package org.zerograph.api;

import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

public interface ResourceInterface {

    public String getName();

    public void get(RequestInterface request) throws Status4xx, Status5xx;

    public void put(RequestInterface request) throws Status4xx, Status5xx;

    public void patch(RequestInterface request) throws Status4xx, Status5xx;

    public void post(RequestInterface request) throws Status4xx, Status5xx;

    public void delete(RequestInterface request) throws Status4xx, Status5xx;

}
