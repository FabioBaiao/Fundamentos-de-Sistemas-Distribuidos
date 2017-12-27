package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

public class Client {
    public static void main(String[] args) throws Exception {
/*
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);

        Connection c = tc.execute(() ->
                t.client().connect(new Address("localhost:10000"))
        ).join().get();

        //  Book b = s.search("one");

        StoreSearchRep r = (StoreSearchRep) tc.execute(() ->
                c.sendAndReceive(new StoreSearchReq("one"))
        ).join().get();

        System.out.println("isbn = "+r.isbn);
       */
    }

}
