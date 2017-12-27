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
import java.util.concurrent.atomic.AtomicInteger;

public class DistObj {
    private Transport t;
    private ThreadContext tc;
    private Map<Integer, Object> objs;
    private Connection c;
    private AtomicInteger id;
    private Address address;

    public DistObj(Address address){
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        t = new NettyTransport();
        tc = new SingleThreadContext("", new Serializer());
        this.address = address;

        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);
        tc.serializer().register(StoreMakeCartReq.class);
        tc.serializer().register(StoreMakeCartRep.class);
        tc.serializer().register(CartAddReq.class);
        tc.serializer().register(CartAddRep.class);
        tc.serializer().register(Book.class);

        tc.execute(() -> t.server().listen(address, c -> {
            c.handler(StoreSearchReq.class, (m) -> {
                LocalStore x = (LocalStore) objs.get(m.id);
                Book b = x.search(m.title);
                System.out.println("qwert");
                return Futures.completedFuture(b);
            });
            c.handler(StoreMakeCartReq.class, (m) -> {
                Store x = (Store) objs.get(m.store_id);
                int cartId = id.getAndIncrement();
                Cart cart = new LocalCart(cartId);
                objs.put(cartId, cart);
                return Futures.completedFuture(
                        new StoreMakeCartRep( address, cartId, Cart.class.getSimpleName()));
            });
            c.handler(CartAddReq.class, (m) -> {
                // TODO: take care of the hardcoded store id
                LocalStore store = (LocalStore) objs.get(0);
                LocalCart cart = (LocalCart) objs.get(m.cartId);
                Book book = store.get(m.isbn);
                cart.addBook(book);
                return Futures.completedFuture(new CartAddRep(0));
            });
            })
        );
    }

    public ObjRef objExport(Object o){
        ObjRef ref = null;
        if(o instanceof Store){
            int i = id.getAndIncrement();
            objs.put(i, (Store) o);
            ref = new ObjRef(address, i, Store.class.getSimpleName());
        }else if(o  instanceof Cart){
            int i = id.getAndIncrement();
            objs.put(i, (Cart) o);
            ref = new ObjRef(address, i, Store.class.getSimpleName());
        }
        return ref;
    }

    public Object objImport(ObjRef ref){
        Object o = null;
        if(ref.address.equals(address.toString())){
            o = objs.get(ref.id);
        }
        else if(ref.cls.equals("Store")){
            try {
                o = new StoreStub(address,this.t, this.tc, this, ref.id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(ref.cls.equals("Cart")){
            o = new CartStub(this.c,this.tc,this,ref.id);
        }
        return o;
    }

}
