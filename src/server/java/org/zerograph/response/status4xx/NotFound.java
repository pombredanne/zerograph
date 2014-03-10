package org.zerograph.response.status4xx;

public class NotFound extends Status4xx {

    public NotFound(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return NOT_FOUND;
    }

}
