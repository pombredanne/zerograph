package com.nigelsmall.zerograph.except;

import com.nigelsmall.zerograph.Response;

public class ServerError extends Exception {

    final private Response response;

    public ServerError(Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }

}
