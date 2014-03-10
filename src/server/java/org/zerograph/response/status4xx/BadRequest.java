package org.zerograph.response.status4xx;

public class BadRequest extends Status4xx {

    public BadRequest(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return BAD_REQUEST;
    }

}
