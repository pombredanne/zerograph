package com.nigelsmall.zerograph;

import com.nigelsmall.zerograph.resources.CypherResource;
import com.nigelsmall.zerograph.resources.NodeResource;
import com.nigelsmall.zerograph.resources.NodeSetResource;
import org.zeromq.ZMQ;

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
                switch (request.getResource()) {
                    case CypherResource.NAME:
                        new CypherResource(env, external).handle(request);
                        break;
                    case NodeResource.NAME:
                        new NodeResource(env, external).handle(request);
                        break;
                    case NodeSetResource.NAME:
                        new NodeSetResource(env, external).handle(request);
                        break;
                    default:
                        new Response(Response.NOT_FOUND, new Object[] { request.getResource() }).send(external);
                }
            } catch (BadRequest badRequest) {
                badRequest.getResponse().send(external);
            }
            System.out.println();
        }
    }

}
