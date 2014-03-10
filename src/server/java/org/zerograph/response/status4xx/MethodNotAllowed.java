package org.zerograph.response.status4xx;

public class MethodNotAllowed extends Status4xx {

    public MethodNotAllowed(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return METHOD_NOT_ALLOWED;
    }

}
