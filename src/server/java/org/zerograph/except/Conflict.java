package org.zerograph.except;

import org.zerograph.Response;

public class Conflict extends ClientError {

    public Conflict(Object... data) {
        super(Response.CONFLICT, data);
    }

}
