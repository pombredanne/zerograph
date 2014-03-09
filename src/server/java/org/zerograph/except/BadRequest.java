package org.zerograph.except;

import org.zerograph.Response;

public class BadRequest extends ClientError {

    public BadRequest(Object... data) {
        super(Response.BAD_REQUEST, data);
    }

}
