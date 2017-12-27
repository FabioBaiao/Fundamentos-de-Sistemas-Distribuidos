package bookstore;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static void main(String[] args) throws Exception {

        String host = "127.0.0.1";
        int port = 10000;

        DistributedObjectsRuntime dor = new DistributedObjectsRuntime(host, port);

        Store store = new LocalStore();

        dor.objExport(store);

        //tc.serializer().register(StoreSearchReq.class);
        //tc.serializer().register(StoreSearchRep.class);
    }
}
