package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

public class RemoteStore implements Store {
    private SingleThreadContext tc;
    private Connection c;

    public RemoteStore() throws Exception {
        Transport t = new NettyTransport();
        tc = new SingleThreadContext("srv-%d", new Serializer());

        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);
        tc.serializer().register(BookIsbnReq.class);
        tc.serializer().register(BookIsbnRep.class);
        tc.serializer().register(BookTitleReq.class);
        tc.serializer().register(BookTitleRep.class);
        tc.serializer().register(BookTitleReq.class);
        tc.serializer().register(BookTitleRep.class);

        c = tc.execute(() ->
                t.client().connect(new Address("localhost:10000"))
        ).join().get();
    }

    @Override
    public RemoteBook get(int isbn) {
        return null;
    }

    public RemoteBook search(String title) throws Exception {
        StoreSearchRep reply = (StoreSearchRep) tc.execute(() ->
                c.sendAndReceive(new StoreSearchReq(title, 0))
        ).join().get();

        return (RemoteBook) Util.makeRemote(tc, reply.getReference());
    }

    public RemoteCart newCart() throws Exception {
        StoreMakeCartRep reply = (StoreMakeCartRep) tc.execute(() ->
            c.sendAndReceive(new StoreMakeCartReq(0))
        ).join().get();

        return (RemoteCart) Util.makeRemote(tc, reply.getReference());
    }

}
