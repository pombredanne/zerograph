package org.zerograph.except;

import org.zerograph.Response;

public class NotFound extends ClientError {

    public NotFound(Object... data) {
        super(Response.NOT_FOUND, data);
    }

}
