package org.zerograph.response.status1xx;

import org.zerograph.api.ResponseInterface;

public abstract class Status1xx implements ResponseInterface {

    final public static int CONTINUE = 100;

    final private Object[] data;

    public Status1xx(Object... data) {
        this.data = data;
    }

    @Override
    public Object[] getData() {
        return data;
    }

}
