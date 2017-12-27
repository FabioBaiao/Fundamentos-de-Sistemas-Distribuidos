package bookstore;

import bookstore.Book;
import bookstore.Cart;
import bookstore.RemoteStore;
import bookstore.Store;
import io.atomix.catalyst.transport.Address;

public class Client {
    public static void main(String[] args) throws Exception {

        String host = "127.0.0.1";
        int port = 20000;

        DistributedObjectsRuntime dor = new DistributedObjectsRuntime(host, port);

        Store store = (Store) dor.objImport(new ObjRef(new Address("127.0.0.1", 10000), 1, "Store"));

        Book b = store.search("one");

        Cart c = store.newCart();

        c.add(b);

        c.buy();
    }
}
