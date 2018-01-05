package bookstore;

import common.DistributedObjectsRuntime;
import io.atomix.catalyst.transport.Address;
import twophasecommit.TransactionContext;

import java.util.concurrent.ExecutionException;
import java.util.SortedSet;

public class RemoteStore implements Store {

    private final DistributedObjectsRuntime dor;
    private final int cliqueId;
    private final int objectId;

    public RemoteStore(DistributedObjectsRuntime dor, int cliqueId,  int objectId) {
        this.dor = dor;
        this.cliqueId = cliqueId;
        this.objectId = objectId;
    }

    @Override
    public Book search(TransactionContext txCtxt, String title) {
        try {
            StoreSearchRep r = (StoreSearchRep) dor.tc.execute(() ->
                    dor.c.sendAndReceive(cliqueId, new StoreSearchReq(txCtxt, objectId, title))
            ).join().get();

            /*StoreSearchRep r = (StoreSearchRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreSearchReq(txCtxt, id, title))
            ).join().get();*/
            
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
                    dor.c.sendAndReceive(cliqueId, new StoreMakeCartReq(txCtxt, objectId, clientId))
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
                    dor.c.sendAndReceive(cliqueId, new StoreGetOrderHistoryReq(txCtxt, objectId, clientId))
            ).join().get();

            return r.getOrderHistory();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
