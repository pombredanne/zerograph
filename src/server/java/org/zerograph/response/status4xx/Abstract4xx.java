package org.zerograph.response.status4xx;

import org.zerograph.api.ResponseInterface;
import org.zerograph.except.ZerographException;

public abstract class Abstract4xx extends ZerographException implements ResponseInterface {

    final public static int BAD_REQUEST = 400;
    final public static int NOT_FOUND = 404;
    final public static int METHOD_NOT_ALLOWED = 405;
    final public static int CONFLICT = 409;

    final private Object[] data;

    public Abstract4xx(Object... data) {
        this.data = data;
    }

    @Override
    public Object[] getData() {
        return data;
    }

}
