package bookstore;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static void main(String[] args) throws Exception {

        Map<Integer, Object> objs = new HashMap<>();
        AtomicInteger id = new AtomicInteger(0);
        Store s = new LocalStore();
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        objs.put(id.getAndIncrement(), s);
        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);
        tc.serializer().register(StoreMakeCartReq.class);
        tc.serializer().register(StoreMakeCartRep.class);
        tc.serializer().register(CartAddReq.class);
        tc.serializer().register(CartAddRep.class);
        tc.serializer().register(BookAuthorReq.class);
        tc.serializer().register(BookAuthorRep.class);
        tc.serializer().register(BookTitleReq.class);
        tc.serializer().register(BookTitleRep.class);
        tc.serializer().register(BookIsbnReq.class);
        tc.serializer().register(BookIsbnRep.class);

        tc.execute(() -> {
            t.server().listen(new Address(":10000"), (c) -> {
                c.handler(StoreSearchReq.class, (m) -> {
                    Store x = (Store) objs.get(m.id);

                    Book b = null;
                    try {
                        b = x.search(m.title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int b_id = id.getAndIncrement();
                    objs.put(b_id,b);
                    return Futures.completedFuture(
                            new StoreSearchRep(b_id, new Address(":10000"), Book.class.getSimpleName())
                    );
                });
                c.handler(StoreMakeCartReq.class, (m) -> {
                    Store x = (Store) objs.get(m.store_id);
                    int cartId = id.getAndIncrement();
                    Cart cart = new LocalCart(cartId);
                    objs.put(cartId, cart);
                    return Futures.completedFuture(
                            new StoreMakeCartRep(new Address(":10000"),cartId, Cart.class.getSimpleName()));
                });
                c.handler(CartAddReq.class, (m) -> {
                    LocalCart cart = (LocalCart) objs.get(m.cartId);
                    Book book = (LocalBook) objs.get(m.bookId);
                    cart.addBook(book);
                    return Futures.completedFuture(new CartAddRep(0));
                });
                c.handler(BookIsbnReq.class, (m) -> {
                    LocalBook b = (LocalBook) objs.get(m.id);
                    return Futures.completedFuture(new BookIsbnRep(b.getIsbn()));
                });
            });
        });
    }
}
