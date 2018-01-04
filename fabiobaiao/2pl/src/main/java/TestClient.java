import Communication.TransactionContext;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;

import java.util.concurrent.ExecutionException;


public class TestClient {

    public static void main(String[] args){

        int id = Integer.parseInt(args[0]);

        ThreadContext tc = new SingleThreadContext("cli-%d", new Serializer());
        Transport t = new NettyTransport();
        Clique c = new Clique(t, id, Common.addresses);

        TransactionContext xContext = begin(tc, c);

        Object o;

        o = method1(tc, c, xContext);

        o = method2(tc, c, xContext);

        commit(tc, c, xContext);
    }

    public static TransactionContext begin(ThreadContext tc, Clique c) {
        try {
            return (TransactionContext) tc.execute(() -> c.sendAndReceive(0, new Begin())).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object method1(ThreadContext tc, Clique c, TransactionContext xContext) {
        try {
            Object o =  tc.execute(() -> c.sendAndReceive(1, new MethodCall(xContext))).join().get();
            if (o instanceof Rollback) {
                // ...
            }
            return o;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object method2(ThreadContext tc, Clique c, TransactionContext xContext) {
        try {
            Object o = tc.execute(() -> c.sendAndReceive(2, new MethodCall(xContext))).join().get();
            if (o instanceof Rollback) {
                // ...
            }
            return o;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void commit(ThreadContext tc, Clique c, TransactionContext xContext) {

        try {
            Object o = tc.execute(() -> c.send(0, new Commit(xContext))).join().get();
            if (o instanceof Rollback) {

            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
