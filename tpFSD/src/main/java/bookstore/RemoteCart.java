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
    private final Address a;
    private final int id;

    public RemoteCart(DistributedObjectsRuntime dor, Address a, int id) {
        this.dor = dor;
        this.a = a;
        this.id = id;
    }

    @Override
    public boolean add(TransactionContext txCtxt, Book b) {
        try {
            CartAddRep r = (CartAddRep) dor.tc.execute(() ->
                dor.cons.get(a).sendAndReceive(new CartAddReq(txCtxt, id, b))
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
                dor.cons.get(a).sendAndReceive(new CartRemoveReq(txCtxt, id, b))
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
                dor.cons.get(a).sendAndReceive(new CartClearReq(txCtxt, id))
            ).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Book> getContent(TransactionContext txCtxt) {
        try {
            CartGetContentRep r = (CartGetContentRep) dor.tc.execute(() ->
                dor.cons.get(a).sendAndReceive(new CartGetContentReq(txCtxt, id))
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
                dor.cons.get(a).sendAndReceive(new CartBuyReq(txCtxt, id, srcRef, paymentDescription))
            ).join().get();
            
            return r.getOrder();
        } catch (InterruptedException | ExecutionException | ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }
}
