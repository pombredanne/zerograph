package org.zerograph.except;

import org.zerograph.Response;

public class MethodNotAllowed extends ClientError {

    public MethodNotAllowed(Object... data) {
        super(Response.METHOD_NOT_ALLOWED, data);
    }

}
