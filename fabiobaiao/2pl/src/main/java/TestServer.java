import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;

public class TestServer {

    public static void main(String[] args){

        int id = Integer.parseInt(args[0]);

        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
        Transport t = new NettyTransport();
        Clique c = new Clique(t, id, Common.addresses);

        ServerRPC rpc = new ServerRPC(tc, c, id);

        rpc.init();

        c.handler(MethodCall.class, (from, recv) -> {

            if (!rpc.register(recv.xContext, from)) {
                return Futures.completedFuture(new Rollback());
            }

            // method
            // append initial status of each modified object ??
            // lock used objects

            // return method result
            return Futures.completedFuture(null);
        });

        tc.execute(() -> c.open());
    }
}
