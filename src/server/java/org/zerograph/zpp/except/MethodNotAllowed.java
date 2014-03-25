package org.zerograph.zpp.except;

public class MethodNotAllowed extends ClientError {

    final private String method;

    public MethodNotAllowed(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

}
