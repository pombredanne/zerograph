package com.nigelsmall.borneo;

import java.io.IOException;

public class BadRequest extends Exception {

    public BadRequest(String message) {
        super(message);
    }

    public Response getResponse() throws IOException {
        return new Response(Response.BAD_REQUEST, new Object[] { this.getMessage() });
    }

}
