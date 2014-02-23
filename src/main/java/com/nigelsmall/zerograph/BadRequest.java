package com.nigelsmall.zerograph;

public class BadRequest extends Exception {

    public BadRequest(String message) {
        super(message);
    }

    public Response getResponse() {
        return new Response(Response.BAD_REQUEST, new Object[] { this.getMessage() });
    }

}
