package org.zerograph.response.status2xx;

import org.zerograph.api.ResponseInterface;

public abstract class Status2xx implements ResponseInterface {

    final public static int OK = 200;
    final public static int CREATED = 201;
    final public static int NO_CONTENT = 204;

    final private Object[] data;

    public Status2xx(Object... data) {
        this.data = data;
    }

    @Override
    public Object[] getData() {
        return data;
    }

}
