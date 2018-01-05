import Communication.RollbackComm;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;

import java.util.concurrent.CompletableFuture;

public class TestServer {

    public static void main(String[] args){

        int id = Integer.parseInt(args[0]);

        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
        Transport t = new NettyTransport();
        //Transport t = NettyTransport.builder().withRequestTimeout(60000).withConnectTimeout(60000).build();
        Clique c = new Clique(t, Clique.Mode.ANY, id, Common.addresses);

        ServerRPC rpc = new ServerRPC(tc, c, id);

        rpc.init();

        tc.execute(() -> {
            c.handler(MethodCall.class, (from, recv) -> {

                return rpc.register(recv.xContext, from, () -> {
                    //method
                    //append initial status of each modified object
                    //lock used object

                    System.out.println("MethodCalled " + System.nanoTime());
                    // return method result
                    return new Reply();
                });

                /*return rpc.addResource(recv.xContext, from).thenApply((res) -> {
                    if (res == false) {
                        return new Rollback();
                    }
                    else {
                        // method
                        // append initial status of each modified object
                        // lock used objects

                        System.out.println("MethodCalled " + System.nanoTime());
                        // return method result
                        return null;
                    }
                });*/
            });
        });

    }
}
