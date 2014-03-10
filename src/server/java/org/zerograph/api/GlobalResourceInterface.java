package org.zerograph.api;

import org.zerograph.Request;
import org.zerograph.response.status4xx.Abstract4xx;
import org.zerograph.response.status5xx.Abstract5xx;

public interface GlobalResourceInterface extends ResourceInterface {

    public void get(Request request) throws Abstract4xx, Abstract5xx;

    public void put(Request request) throws Abstract4xx, Abstract5xx;

    public void patch(Request request) throws Abstract4xx, Abstract5xx;

    public void post(Request request) throws Abstract4xx, Abstract5xx;

    public void delete(Request request) throws Abstract4xx, Abstract5xx;

}
