package bookstore;

import bank.Bank;
import bank.RemoteBank;
import common.DistributedObjectsRuntime;
import common.ObjRef;
import io.atomix.catalyst.transport.Address;
import twophasecommit.TransactionContext;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class RemoteCart implements Cart {

    private final DistributedObjectsRuntime dor;
    private final int cliqueId;
    private final int objectId;

    public RemoteCart(DistributedObjectsRuntime dor, int cliqueId, int objectId) {
        this.dor = dor;
        this.cliqueId = cliqueId;
        this.objectId = objectId;
    }

    @Override
    public boolean add(TransactionContext txCtxt, Book b) {
        try {
            CartAddRep r = (CartAddRep) dor.tc.execute(() ->
                dor.c.sendAndReceive(cliqueId, new CartAddReq(txCtxt, objectId, b))
            ).join().get();
            
            return r.getAdded();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean remove(TransactionContext txCtxt, Book b) {
        try {
            CartRemoveRep r = (CartRemoveRep) dor.tc.execute(() ->
                dor.c.sendAndReceive(cliqueId, new CartRemoveReq(txCtxt, objectId, b))
            ).join().get();
            
            return r.removed;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void clear(TransactionContext txCtxt) {
        try {
            CartClearRep r = (CartClearRep) dor.tc.execute(() ->
                dor.c.sendAndReceive(cliqueId, new CartClearReq(txCtxt, objectId))
            ).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Book> getContent(TransactionContext txCtxt) {
        try {
            CartGetContentRep r = (CartGetContentRep) dor.tc.execute(() ->
                dor.c.sendAndReceive(cliqueId, new CartGetContentReq(txCtxt, objectId))
            ).join().get();

            return r.content;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Order buy(TransactionContext txCtxt, Bank.Account srcAccount, String paymentDescription) {
        try {
            RemoteBank.RemoteAccount remoteSrc = (RemoteBank.RemoteAccount) srcAccount;
            ObjRef srcRef = remoteSrc.getObjRef();

            CartBuyRep r = (CartBuyRep) dor.tc.execute(() ->
                dor.c.sendAndReceive(cliqueId, new CartBuyReq(txCtxt, objectId, srcRef, paymentDescription))
            ).join().get();
            
            return r.getOrder();
        } catch (InterruptedException | ExecutionException | ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }
}
