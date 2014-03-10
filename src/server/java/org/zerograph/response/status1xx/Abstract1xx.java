package org.zerograph.response.status1xx;

import org.zerograph.api.ResponseInterface;

public abstract class Abstract1xx implements ResponseInterface {

    final public static int CONTINUE = 100;

    final private Object[] data;

    public Abstract1xx(Object... data) {
        this.data = data;
    }

    @Override
    public Object[] getData() {
        return data;
    }

}
