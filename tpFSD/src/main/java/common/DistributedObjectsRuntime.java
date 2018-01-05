package common;

import bank.Bank;
import bank.BankGetAccountRep;
import bank.BankGetAccountReq;
import bank.RemoteBank;
import bookstore.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import twophasecommit.Begin;
import twophasecommit.Commit;
import twophasecommit.Participant;
import twophasecommit.TransactionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjectsRuntime {

    public ThreadContext tc;
    Transport t;
    public Clique c;
    int cliqueId;

    Map<Integer, Object> objs;
    AtomicInteger id;

    Participant p;

    private static final int COORDINATOR_ID = 0;

    public DistributedObjectsRuntime(int id) {
        this.tc = new SingleThreadContext("srv-%d", new Serializer());
        this.t = new NettyTransport();
        this.cliqueId = id;
        this.c = new Clique(t, Clique.Mode.ANY, id, Common.addresses);

        this.objs = new HashMap<>();
        this.id = new AtomicInteger(0);

        this.p = new Participant(tc, c, id);
    }

    public void init() {
        try {
            Common.registerSerializers(tc);
            p.init();
            initializeHandlers();
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void initializeHandlers() {
        tc.execute(() -> {
            c.handler(StoreSearchReq.class, (from, r) -> {

                return p.register(r.getTransactionContext(), from, () -> {

                    Store s = (Store) objs.get(r.getObjId());

                    Book b = s.search(r.getTransactionContext(), r.getTitle());
//                  int bookid = objectId.incrementAndGet();
//                  objs.put(bookid, b);

                    return new StoreSearchRep(b);
                });
            });

            c.handler(BankGetAccountReq.class, (from, r) -> {

                return p.register(r.getTransactionContext(), from, () -> {

                    Bank b = (Bank) objs.get(r.getObjId());

                    Bank.Account a = null;
                    try {
                        a = b.getAccount(r.getTransactionContext(), r.getAccountNo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int accountId = id.incrementAndGet();
                    objs.put(accountId, a);

                    return new BankGetAccountRep(new ObjRef(cliqueId, accountId, "Account"));
                });
            });

            c.handler(StoreMakeCartReq.class, (from, r) -> {
                Store s = (Store) objs.get(r.getObjId());

                int cartId = id.incrementAndGet();
                objs.put(cartId, s.newCart(r.getTransactionContext(), r.getClientId()));

                return Futures.completedFuture(new StoreMakeCartRep(new ObjRef(cliqueId, cartId, "Cart")));
            });

            c.handler(CartAddReq.class, (from, r) -> {
                Cart cart = (Cart) objs.get(r.getObjId());

                boolean added = cart.add(r.getTransactionContext(), r.getBook());
                return Futures.completedFuture(new CartAddRep(added));
            });

            c.handler(CartBuyReq.class, (from, r) -> {
                Cart cart = (Cart) objs.get(r.getObjId());
                Bank.Account srcAccount = (Bank.Account) objImport(r.getSrcAccountRef());
                Order o = cart.buy(r.getTransactionContext(), srcAccount, r.getDescription());

                return Futures.completedFuture(new CartBuyRep(o));
            });
        });
    }

    public ObjRef objExport(Object o){
        int objid = this.id.incrementAndGet();
        this.objs.put(objid, o);
        if (o instanceof Store)
            return new ObjRef(cliqueId, objid, "Store");
        else
            return null;
    }

    public Object objImport(ObjRef ref){
        if (this.cliqueId == ref.cliqueId){
            return this.objs.get(ref.objectId);
        }
        /*if (!cons.containsKey(ref.cliqueId))
            try {
                Connection c = tc.execute(() ->
                        t.client().connect(ref.cliqueId)
                ).join().get();
                cons.put(ref.cliqueId, c);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
        }*/

        switch (ref.cls){

            case "Bank":
                return new RemoteBank(this, ref.cliqueId, ref.objectId);
            case "Account":
                return new RemoteBank.RemoteAccount(this, ref.cliqueId, ref.objectId);
            case "Store":
                return new RemoteStore(this, ref.cliqueId, ref.objectId);
            case "Cart":
                return new RemoteCart(this, ref.cliqueId, ref.objectId);
/*
            case "Book": // A classe Book é imutável
                return new RemoteBook(this, ref.cliqueId, ref.objectId);
*/
        }
        return null;
    }

    public TransactionContext begin() throws ExecutionException, InterruptedException {
        return (TransactionContext) tc.execute(() -> c.sendAndReceive(COORDINATOR_ID, new Begin())).join().get();
    }

    public Object commit(TransactionContext xContext) throws ExecutionException, InterruptedException {
        return tc.execute(() -> c.sendAndReceive(COORDINATOR_ID, new Commit(xContext))).join().get();
    }

}
