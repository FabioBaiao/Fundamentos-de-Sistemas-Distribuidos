package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;

import java.util.concurrent.ExecutionException;

public class RemoteCart implements Cart {

    DistributedObjectsRuntime dor;

    Address a;

    private int id;

    RemoteCart(DistributedObjectsRuntime dor, Address a, int id) {
        this.dor = dor;
        this.a = a;
        this.id = id;
    }

    @Override
    public boolean add(Book b) {
        try {
            RemoteBook rb = (RemoteBook) b;
            ObjRef ref = new ObjRef(rb.a, rb.id, "Book");
            CartAddRep r = (CartAddRep) dor.tc.execute(() ->
                dor.cons.get(a).sendAndReceive(new CartAddReq(id, ref))
            ).join().get();
            return r.result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean buy() {
        try {
            CartBuyRep r = (CartBuyRep) dor.tc.execute(() ->
                dor.cons.get(a).sendAndReceive(new CartBuyReq(id))
            ).join().get();
            return r.result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
