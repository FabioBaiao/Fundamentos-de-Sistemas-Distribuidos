package bookstore;

import io.atomix.catalyst.transport.Address;

import java.util.concurrent.ExecutionException;
import java.util.SortedSet;

public class RemoteStore implements Store {

    private final DistributedObjectsRuntime dor;
    private final Address a;
    private final int id;

    public RemoteStore(DistributedObjectsRuntime dor, Address a,  int id) {
        this.dor = dor;
        this.a = a;
        this.id = id;
    }

    @Override
    public Book search(String title) {
        try {
            StoreSearchRep r = (StoreSearchRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreSearchReq(id, title))
            ).join().get();
            
            return r.book;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Cart newCart(int clientId) {
        try {
            StoreMakeCartRep r = (StoreMakeCartRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreMakeCartReq(id, clientId))
            ).join().get();
            
            return (Cart) dor.objImport(r.ref);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SortedSet<Order> getOrderHistory(int clientId) {
        try {
            StoreGetOrderHistoryRep r = (StoreGetOrderHistoryRep) dor.tc.execute(() -> 
                    dor.cons.get(a).sendAndReceive(new StoreGetOrderHistoryReq(id, clientId))
            ).join().get();

            return r.orderHistory;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
