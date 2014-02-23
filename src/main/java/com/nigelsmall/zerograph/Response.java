package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.util.JSON;
import org.zeromq.ZMQ;

import java.io.IOException;

/**
 * Response is tab-separated string of terms
 *
 * STATUS [data [data ...]]
 *
 */
public class Response {

    // Borrowed from HTTP
    final public static int CONTINUE = 100;
    final public static int OK = 200;
    final public static int BAD_REQUEST = 400;
    final public static int NOT_FOUND = 404;
    final public static int METHOD_NOT_ALLOWED = 405;
    final public static int SERVER_ERROR = 500;
    final public static int NOT_IMPLEMENTED = 501;

    final private String string;
    final private int status;
    final private Object[] data;

    public Response(int status, Object... data) {
        this.status = status;
        this.data = data;
        StringBuilder builder = new StringBuilder(Integer.toString(status));
        for (Object datum : data) {
            builder.append('\t');
            try {
                builder.append(JSON.encode(datum));
            } catch (IOException ex) {
                builder.append('?');  // TODO
            }
        }
        this.string = builder.toString();
    }

    public Response(int status) {
        this(status, new Object[0]);
    }

    public String toString() {
        return this.string;
    }

    public int getStatus() {
        return this.status;
    }

    public Object[] getData() {
        return this.data;
    }

    public boolean send(ZMQ.Socket socket) {
        String string = this.toString();
        System.out.println(">>> " + string);

        int flags = this.status / 100 == 1 ? ZMQ.SNDMORE : 0;
        return socket.send(string.getBytes(ZMQ.CHARSET), flags);

    }

}
