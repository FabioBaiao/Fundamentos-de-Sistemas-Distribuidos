package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class StoreStub implements Store{
    private Connection c;
    private ThreadContext tc;
    private DistObj DO;
    private int id;

    public StoreStub(Address address, Transport t, ThreadContext tc, DistObj DO, int id) throws Exception {
        this.c = tc.execute(() -> t.client().connect(address)).join().get();
        this.tc = tc;
        this.DO = DO;
        this.id = id;
    }

    @Override
    public Book get(int isbn) {
        return null;
    }

    @Override
    public Book search(String title) throws Exception {
        Book rep = (Book) tc.execute(() ->
            c.sendAndReceive(new StoreSearchReq(title, id))
        ).join().get();
        return rep;
    }

    @Override
    public Cart newCart() throws Exception {
        StoreMakeCartRep rep = (StoreMakeCartRep) tc.execute(() -> {
            return c.sendAndReceive(new StoreMakeCartReq(id));
        }).join().get();
        return (Cart) DO.objImport(rep.getReference());
    }
}
