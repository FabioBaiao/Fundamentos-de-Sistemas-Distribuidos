import Communication.BeginComm;
import Communication.CommitComm;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;


public class TestClient {

    public static void main(String[] args){

        ThreadContext tc = new SingleThreadContext("cli-%d", new Serializer());
        Transport t = new NettyTransport();

        int xid = begin(tc, t);

        Object o;

        o = method1(tc, t, xid);

        o = method2(tc, t, xid);

        commit(tc, t, xid);
    }

    public static int begin(ThreadContext tc, Transport t) {
        Connection coordConn = tc.execute(() ->
                t.client().connect(new Address("127.0.0.1", 10000))
        ).join().join();


        return (int) tc.execute(() ->
                coordConn.sendAndReceive(new BeginComm())
        ).join().join();
    }

    public static Object method1(ThreadContext tc, Transport t, int xid) {
        Connection srv1Conn = tc.execute(() ->
                t.client().connect(new Address("127.0.0.1", 10001))
        ).join().join();

        return tc.execute(() ->
                srv1Conn.sendAndReceive(new MethodCall(xid))
        ).join().join();
    }

    public static Object method2(ThreadContext tc, Transport t, int xid) {
        Connection srv1Conn = tc.execute(() ->
                t.client().connect(new Address("127.0.0.1", 10002))
        ).join().join();

        return tc.execute(() ->
                srv1Conn.sendAndReceive(new MethodCall(xid))
        ).join().join();
    }

    public static void commit(ThreadContext tc, Transport t, int xid) {
        Connection coordConn = tc.execute(() ->
                t.client().connect(new Address("127.0.0.1", 10000))
        ).join().join();

        tc.execute(() ->
                coordConn.send(new CommitComm(xid))
        ).join().join();
    }
}
