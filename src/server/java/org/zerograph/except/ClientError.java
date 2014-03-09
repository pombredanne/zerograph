package org.zerograph.except;

import org.zerograph.Response;

public class ClientError extends Exception {

    final private Response response;

    public ClientError(int status, Object... data) {
        super();
        this.response = new Response(status, data);
    }

    public Response getResponse() {
        return this.response;
    }

}
