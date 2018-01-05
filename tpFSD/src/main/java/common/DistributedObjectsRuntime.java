package common;

import bank.Bank;
import bookstore.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjectsRuntime {

    public ThreadContext tc;
    Transport t;
    Address address;

    Map<Integer, Object> objs;
    AtomicInteger id;

    public Map<Address, Connection> cons;

    public DistributedObjectsRuntime(String host, int port) {
        this.tc = new SingleThreadContext("srv-%d", new Serializer());
        this.t = new NettyTransport();
        this.address = new Address(host, port);

        this.objs = new HashMap<>();
        this.id = new AtomicInteger(0);

        this.cons = new HashMap<>();

        initializeHandlers();
    }

    private void initializeHandlers() {
        tc.execute(() -> {
            t.server().listen(address, (c) -> {

                c.handler(StoreSearchReq.class, (r) -> {
                    Store s = (Store) objs.get(r.getObjId());

                    Book b = s.search(r.getTitle());
//                  int bookid = id.incrementAndGet();
//                  objs.put(bookid, b);

                    return Futures.completedFuture(new StoreSearchRep(b));
                });

                c.handler(StoreMakeCartReq.class, (r) -> {
                    Store s = (Store) objs.get(r.getObjId());

                    int cartId = id.incrementAndGet();
                    objs.put(cartId, s.newCart(r.getClientId()));

                    return Futures.completedFuture(new StoreMakeCartRep(new ObjRef(address, cartId, "Cart")));
                });

                c.handler(CartAddReq.class, (r) -> {
                    Cart cart = (Cart) objs.get(r.getObjId());

                    boolean added = cart.add(r.getBook());
                    return Futures.completedFuture(new CartAddRep(added));
                });

                c.handler(CartBuyReq.class, (r) -> {
                    Cart cart = (Cart) objs.get(r.getObjId());
                    Bank.Account srcAccount = (Bank.Account) objImport(r.getSrcAccountRef());
                    Order o = cart.buy(srcAccount, r.getDescription());

                    return Futures.completedFuture(new CartBuyRep(o));
                });
            });
        });
    }

    public ObjRef objExport(Object o){
        int objid = this.id.incrementAndGet();
        this.objs.put(objid, o);
        if (o instanceof Store)
            return new ObjRef(this.address, objid, "Store");
        else
            return null;
    }

    public Object objImport(ObjRef ref){
        if (this.address.equals(ref.address)){
            return this.objs.get(ref.id);
        }
        if (!cons.containsKey(ref.address))
            try {
                Connection c = tc.execute(() ->
                        t.client().connect(ref.address)
                ).join().get();
                cons.put(ref.address, c);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
        }

        switch (ref.cls){

            case "Store":
                return new RemoteStore(this, ref.address, ref.id);
            case "Cart":
                return new RemoteCart(this, ref.address, ref.id);
/*
            case "Book": // A classe Book é imutável
                return new RemoteBook(this, ref.address, ref.id);
*/
        }
        return null;
    }

}
