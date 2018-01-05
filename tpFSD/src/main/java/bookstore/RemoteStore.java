package bookstore;

import common.DistributedObjectsRuntime;
import io.atomix.catalyst.transport.Address;
import twophasecommit.TransactionContext;

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
    public Book search(TransactionContext txCtxt, String title) {
        try {
            StoreSearchRep r = (StoreSearchRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreSearchReq(txCtxt, id, title))
            ).join().get();
            
            return r.getBook();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Cart newCart(TransactionContext txCtxt, int clientId) {
        try {
            StoreMakeCartRep r = (StoreMakeCartRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreMakeCartReq(txCtxt, id, clientId))
            ).join().get();
            
            return (Cart) dor.objImport(r.getCartRef());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SortedSet<Order> getOrderHistory(TransactionContext txCtxt, int clientId) {
        try {
            StoreGetOrderHistoryRep r = (StoreGetOrderHistoryRep) dor.tc.execute(() -> 
                    dor.cons.get(a).sendAndReceive(new StoreGetOrderHistoryReq(txCtxt, id, clientId))
            ).join().get();

            return r.getOrderHistory();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
