package common;

import bank.*;
import bookstore.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import twophasecommit.Begin;
import twophasecommit.Commit;
import twophasecommit.Participant;
import twophasecommit.TransactionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjectsRuntime {

    private static final int COORDINATOR_ID = 0;

    public ThreadContext tc;
    public Clique c;
    Transport t;
    int cliqueId;

    Map<Integer, Object> objs;
    AtomicInteger nextId;

    Participant p;

    public DistributedObjectsRuntime(int cliqueId) {
        this.tc = new SingleThreadContext("srv-%d", new Serializer());
        this.t = new NettyTransport();
        this.cliqueId = cliqueId;
        this.c = new Clique(t, Clique.Mode.ANY, cliqueId, Common.addresses);

        this.objs = new HashMap<>();
        this.nextId = new AtomicInteger(0);

        this.p = new Participant(tc, c, cliqueId);
    }

    public void init() {
        try {
            Common.registerSerializers(tc);
            p.init();
            storeHandlers();
            cartHandlers();
            bankHandlers();
            accountHandlers();
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    // Store invocation handlers
    private void storeHandlers() {
        tc.execute(() -> {
            c.handler(StoreSearchReq.class, (from, r) -> {
                return p.register(r.getTransactionContext(), from, () -> {
                    Store s = (Store) objs.get(r.getObjId());
                    Book b = s.search(r.getTransactionContext(), r.getTitle());

                    return Futures.completedFuture(new StoreSearchRep(b));
                });
            });

            c.handler(StoreMakeCartReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Store s = (Store) objs.get(r.getObjId());

                    return p.acquire(txCtxt, s).thenCompose((v) -> {
                        int cartId = nextId.incrementAndGet();
                        objs.put(cartId, s.newCart(txCtxt, r.getClientId()));

                        return Futures.completedFuture(new StoreMakeCartRep(new ObjRef(cliqueId, cartId, "Cart")));
                    });
                });
            });

            c.handler(StoreGetOrderHistoryReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Store s = (Store) objs.get(r.getObjId());

                    return p.acquire(txCtxt, s).thenCompose((v) -> {
                        SortedSet<Order> orderHistory = s.getOrderHistory(txCtxt, r.getClientId());
                        return Futures.completedFuture(new StoreGetOrderHistoryRep(orderHistory));
                    });
                });
            });
        });
    }

    // Cart invocation handlers
    private void cartHandlers() {
        tc.execute(() -> {
            c.handler(CartAddReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Cart cart = (Cart) objs.get(r.getObjId());
                    boolean added = cart.add(txCtxt, r.getBook());

                    return Futures.completedFuture(new CartAddRep(added));
                });
            });

            c.handler(CartBuyReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Cart cart = (Cart) objs.get(r.getObjId());
                    Bank.Account srcAccount = (Bank.Account) objImport(r.getSrcAccountRef());
                    Store store = ((LocalStore.LocalCart) cart).getStore();

                    // Locking the store is required for updating the orderHistory Map
                    return p.acquire(txCtxt, store).thenCompose(v -> {
                        Order o = cart.buy(txCtxt, srcAccount, r.getDescription());
                        return Futures.completedFuture(new CartBuyRep(o));
                    });
                });
            });

            c.handler(CartClearReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Cart cart = (Cart) objs.get(r.getObjId());
                    cart.clear(txCtxt);

                    return Futures.completedFuture(new CartClearRep());
                });
            });

            c.handler(CartGetContentReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Cart c = (Cart) objs.get(r.getObjId());

                    return Futures.completedFuture(new CartGetContentRep(c.getContent(txCtxt)));
                });

            });

            c.handler(CartRemoveReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Cart cart = (Cart) objs.get(r.getObjId());
                    boolean removed = cart.remove(txCtxt, r.getBook());

                    return Futures.completedFuture(new CartRemoveRep(removed));
                });
            });
        });
    }

    // Bank handlers
    private void bankHandlers() {
        tc.execute(() -> {
            c.handler(BankGetAccountReq.class, (from, r) -> {
                return p.register(r.getTransactionContext(), from, () -> {
                    Bank b = (Bank) objs.get(r.getObjId());
                    Bank.Account a = null;
                    try {
                        a = b.getAccount(r.getTransactionContext(), r.getAccountNo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int accountId = nextId.incrementAndGet();
                    objs.put(accountId, a);

                    return Futures.completedFuture(new BankGetAccountRep(new ObjRef(cliqueId, accountId, "Bank.Account")));
                });
            });

            c.handler(BankGetNameReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank b = (Bank) objs.get(r.getObjId());

                    return Futures.completedFuture(new BankGetNameRep(b.getName(txCtxt), null));
                });
            });
        });
    }


    // Account handlers
    private void accountHandlers() {
        tc.execute(() -> {
            c.handler(AccountCreditReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account a = (Bank.Account) objs.get(r.getObjId());

                    return p.acquire(txCtxt, a).thenCompose(v -> {
                        a.credit(txCtxt, r.getAmount());
                        return Futures.completedFuture(new AccountCreditRep());
                    });
                });
            });

            c.handler(AccountDebitReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account a = (Bank.Account) objs.get(r.getObjId());

                    return p.acquire(txCtxt, a).thenCompose(v -> {
                        a.debit(txCtxt, r.getAmount());
                        return Futures.completedFuture(new AccountDebitRep());
                    });
                });
            });

            c.handler(AccountGetNoReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account a = (Bank.Account) objs.get(r.getObjId());
                    return Futures.completedFuture(new AccountGetNoRep(a.getNo(txCtxt)));
                });
            });

            c.handler(AccountGetBalanceReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account a = (Bank.Account) objs.get(r.getObjId());

                    return p.acquire(txCtxt, a).thenCompose(v ->
                        Futures.completedFuture(new AccountGetBalanceRep(a.getBalance(txCtxt)))
                    );
                });
            });

            c.handler(AccountGetPaymentHistoryReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account a = (Bank.Account) objs.get(r.getObjId());

                    return p.acquire(txCtxt, a).thenCompose(v ->
                        Futures.completedFuture(new AccountGetPaymentHistoryRep(a.getPaymentHistory(txCtxt)))
                    );
                });
            });

            c.handler(AccountPayReq.class, (from, r) -> {
                TransactionContext txCtxt = r.getTransactionContext();

                return p.register(txCtxt, from, () -> {
                    Bank.Account srcAccount = (Bank.Account) objs.get(r.getObjId());
                    Bank.Account dstAccount = (Bank.Account) objImport(r.getDstRef());

                    return p.acquire(txCtxt, srcAccount).thenCompose(v ->
                        p.acquire(txCtxt, dstAccount)
                    ).thenCompose(v -> {
                        srcAccount.pay(txCtxt, r.getAmount(), r.getDescription(), dstAccount);
                        return Futures.completedFuture(new AccountPayRep());
                    });
                });
            });
        });
    }

    public ObjRef objExport(Object o){
        int objId = this.nextId.incrementAndGet();
        this.objs.put(objId, o);

        if (o instanceof Store)
            return new ObjRef(cliqueId, objId, "Store");
        else if (o instanceof Bank)
            return new ObjRef(cliqueId, objId, "Bank");
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
            case "Bank.Account":
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

    public Object commit(TransactionContext txCtxt) throws ExecutionException, InterruptedException {
        return tc.execute(() -> c.sendAndReceive(COORDINATOR_ID, new Commit(txCtxt))).join().get();
    }
}
