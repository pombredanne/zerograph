package org.zerograph.test;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.zerograph.api.ZerographInterface;
import org.zeromq.ZMQ;

public class ResourceTest {

    protected ZerographInterface fakeZerograph;
    protected GraphDatabaseService fakeDatabase;
    protected ZMQ.Context fakeContext;
    protected ZMQ.Socket fakeClient;
    protected ZMQ.Socket fakeServer;

    @Before
    public void setUp() {
        fakeZerograph = new FakeZerograph();
        fakeDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        fakeContext = ZMQ.context(1);
        fakeServer = fakeContext.socket(ZMQ.REP);
        fakeServer.bind("inproc://test");
        fakeClient = fakeContext.socket(ZMQ.REQ);
        fakeClient.connect("inproc://test");
        fakeClient.send("");
        fakeServer.recv();
    }

    @After
    public void tearDown() {
        fakeClient.close();
        fakeServer.close();
        fakeDatabase.shutdown();
    }

    public void sendClose() {
        fakeServer.send("");
    }

}
