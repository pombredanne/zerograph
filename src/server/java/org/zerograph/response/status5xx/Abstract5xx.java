package org.zerograph.response.status5xx;

import org.zerograph.api.ResponseInterface;
import org.zerograph.except.ZerographException;

public abstract class Abstract5xx extends ZerographException implements ResponseInterface {

    final public static int SERVER_ERROR = 500;
    final public static int NOT_IMPLEMENTED = 501;

    final private Object[] data;

    public Abstract5xx(Object... data) {
        this.data = data;
    }

    @Override
    public Object[] getData() {
        return data;
    }
}
