package com.nigelsmall.zerograph.except;

import com.nigelsmall.zerograph.Response;

public class ClientError extends Exception {

    final private Response response;

    public ClientError(Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }

}
