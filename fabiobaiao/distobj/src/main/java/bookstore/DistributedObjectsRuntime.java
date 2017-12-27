package bookstore;

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

    ThreadContext tc;
    Transport t;
    Address address;

    Map<Integer, Object> objs;
    AtomicInteger id;

    Map<Address, Connection> cons;

    DistributedObjectsRuntime(String host, int port) {
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

                c.handler(StoreSearchReq.class, (m) -> {
                    Store s = (Store) objs.get(m.storeid);

                    Book b = s.search(m.title);
                    int bookid = id.incrementAndGet();
                    objs.put(bookid, b);

                    return Futures.completedFuture(new StoreSearchRep(new ObjRef(address, bookid, "Book")));
                });

                c.handler(StoreMakeCartReq.class, (m) -> {
                    Store s = (Store) objs.get(m.storeid);

                    int cartid = id.incrementAndGet();
                    objs.put(cartid, s.newCart());

                    return Futures.completedFuture(new StoreMakeCartRep(new ObjRef(address, cartid, "Cart")));
                });

                c.handler(CartAddReq.class, (m) -> {
                    Cart cart = (Cart) objs.get(m.cartid);
                    Book b = (Book) objImport(m.bookRef);

                    return Futures.completedFuture(new CartAddRep(cart.add(b)));
                });

                c.handler(CartBuyReq.class, (m) -> {
                    Cart cart = (Cart) objs.get(m.cartid);

                    return Futures.completedFuture(new CartBuyRep(cart.buy()));
                });
            });
        });
    }

    ObjRef objExport(Object o){
        int objid = this.id.incrementAndGet();
        this.objs.put(objid, o);
        if (o instanceof Store)
            return new ObjRef(this.address, objid, "Store");
        else
            return null;
    }

    Object objImport(ObjRef ref){
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

            case "Book":
                return new RemoteBook(this, ref.address, ref.id);

            case "Cart":
                return new RemoteCart(this, ref.address, ref.id);
        }
        return null;
    }

}
