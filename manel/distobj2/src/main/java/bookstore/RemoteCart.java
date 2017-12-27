package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteCart implements Cart{
    private Transport t;
    private Address addr;
    private ThreadContext tc;
    private Connection c;
    private int id;

    public RemoteCart(int id, Address addr) throws Exception{
        this.id = id;
        this.addr = addr;

        this.t = new NettyTransport();
        this.tc = new SingleThreadContext("rmtCart-%d", new Serializer());
        this.c = tc.execute(() -> t.client().connect(addr)).join().get();
    }

    public void addBook(Book book) throws Exception{
        CartAddRep r = (CartAddRep) tc.execute(() ->
                c.sendAndReceive(new CartAddReq(book.getIsbn(), id))
        ).join().get();
    }

    @Override
    public boolean buy() {
        return false;
    }

}
