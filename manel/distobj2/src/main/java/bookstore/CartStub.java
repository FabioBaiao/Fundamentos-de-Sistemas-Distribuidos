package bookstore;

import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;

public class CartStub implements Cart{
    private Connection c;
    private ThreadContext tc;
    private DistObj DO;
    private int id;

    public CartStub(Connection c, ThreadContext tc, DistObj DO, int id){
        this.c = c;
        this.tc = tc;
        this.DO = DO;
        this.id = id;
    }

    @Override
    public void addBook(Book b) throws Exception {
        CartAddRep rep = (CartAddRep) tc.execute(() ->
            c.sendAndReceive(new CartAddReq(b.getIsbn(),this.id))
        ).join().get();
    }

    @Override
    public boolean buy() {
        return false;
    }
}
