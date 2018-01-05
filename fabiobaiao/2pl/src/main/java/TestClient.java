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
        //Transport t = NettyTransport.builder().withRequestTimeout(60000).withConnectTimeout(60000).build();
        Transport t = new NettyTransport();
        Clique c = new Clique(t, Clique.Mode.ANY, id, Common.addresses);

        Common.registerSerializers(tc);

        try {
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        TransactionContext xContext = begin(tc, c);

        Object o;

        o = method1(tc, c, xContext);

        o = method2(tc, c, xContext);

        commit(tc, c, xContext);
    }

    public static TransactionContext begin(ThreadContext tc, Clique c) {
        try {
            return (TransactionContext) tc.execute(() -> c.sendAndReceive(0, new Begin())).join().get();
        }
        catch (InterruptedException | ExecutionException e) {
            // continuar a tentar até estar conectado ao coordenador (IOException)
            retry(e);
            return begin(tc, c);
        }
    }

    private static void retry(Exception e) {
        System.out.println(e.getMessage());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public static Object method1(ThreadContext tc, Clique c, TransactionContext xContext) {
        try {
            Object o =  tc.execute(() -> c.sendAndReceive(1, new MethodCall(xContext))).join().get();
            if (o instanceof Reply) {
                System.out.println(System.nanoTime());
            }
            else if (o instanceof Rollback) {
                System.out.println("Rollback");
            }

            return o;
        } catch (InterruptedException | ExecutionException e) {
            // continuar a tentar até handler estar registado (SerializationException)
            System.out.print("Method1 ");
            retry(e);
            return method1(tc, c, xContext);
        }
    }

    public static Object method2(ThreadContext tc, Clique c, TransactionContext xContext) {
        try {
            Object o = tc.execute(() -> c.sendAndReceive(2, new MethodCall(xContext))).join().get();
            System.out.println(System.nanoTime());
            if (o instanceof Rollback) {
                // ...
            }
            return o;
        } catch (InterruptedException | ExecutionException e) {
            System.out.print("Method2 ");
            retry(e);
            return method2(tc, c, xContext);
        }
    }

    public static void commit(ThreadContext tc, Clique c, TransactionContext xContext) {

        try {
            Object o = tc.execute(() -> c.sendAndReceive(0, new Commit(xContext))).join().get();
            if (o instanceof Rollback) {
                System.out.println("Rollback " + ((Rollback) o).xid);
            }
            else if (o instanceof Ack) {
                System.out.println("Commited " + System.nanoTime());
            }
            else {
                System.out.println("???");
            }
        } catch (InterruptedException | ExecutionException e) {
            retry(e);
            commit(tc, c, xContext);
        }
    }
}
