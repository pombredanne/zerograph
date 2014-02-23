package com.nigelsmall.borneo;

import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.regex.Pattern;

public class Worker implements Runnable {

    final public static String ADDRESS = "inproc://workers";

    private Environment env;
    private ZMQ.Socket external;

    public Worker(Environment env) {
        this.env = env;
        this.external = env.getContext().socket(ZMQ.REP);
        this.external.connect(ADDRESS);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String string = new String(external.recv(0), ZMQ.CHARSET);
            System.out.println("<<< " + string);
            try {
                Request request = new Request(string);
                handle(request);
            } catch (BadRequest badRequest) {
                badRequest.getResponse().send(external);
            } catch (IOException ex) {
                new Response(Response.SERVER_ERROR).send(external);
            }
            System.out.println();
        }
    }

    public void handle(Request request) throws IOException {
        if (Pattern.matches(CypherResource.PATTERN, request.getResource())) {
            CypherResource cypher = new CypherResource(env, external);
            cypher.handle(request);
        } else {
            new Response(Response.NOT_FOUND, new Object[] {request.getResource()}).send(external);
        }
    }

}
