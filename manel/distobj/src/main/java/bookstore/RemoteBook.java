package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.*;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteBook implements Book{
    private int id;
    private Address addr;
    private Transport t;
    private ThreadContext tc;
    private Connection c;

    public RemoteBook(ThreadContext tc, int id, Address addr) throws Exception {
        this.id = id;
        this.addr = addr;
        this.t = new NettyTransport();
        this.tc = tc; //new SingleThreadContext("", new Serializer());
        this.c = tc.execute(() -> t.client().connect(new Address("localhost:10000"))).join().get();
    }

    public int getId(){
        return id;
    }

    @Override
    public int getIsbn() throws Exception {
        BookIsbnRep rep = (BookIsbnRep) tc.execute(() ->
            c.sendAndReceive(new BookIsbnReq(this.id))
        ).join().get();
        return rep.isbn;
    }

    @Override
    public String getTitle() throws Exception {
        BookTitleRep rep = (BookTitleRep) tc.execute(() ->
                c.sendAndReceive(new BookTitleReq(this.id))
        ).join().get();
        return rep.title;
    }

    @Override
    public String getAuthor() throws ExecutionException, InterruptedException {
        BookAuthorRep rep = (BookAuthorRep) tc.execute(() ->
                c.sendAndReceive(new BookAuthorReq(this.id))
        ).join().get();
        return rep.author;
    }
}
