import bookstore.Book;
import bookstore.Cart;
import bookstore.Store;
import common.Common;
import common.DistributedObjectsRuntime;
import common.ObjRef;
import io.atomix.catalyst.transport.Address;
import twophasecommit.TransactionContext;

import java.util.concurrent.ExecutionException;

public class Client {
    public static void main(String[] args) {

        int id = Integer.parseInt(args[0]);

        DistributedObjectsRuntime dor = new DistributedObjectsRuntime(id);

        dor.init();

        Store store = (Store) dor.objImport(new ObjRef(Common.STORE_SERVER_ID, Common.STORE_ID, "Store"));

        TransactionContext xContext = null;

        try {
            xContext = dor.begin();
        } catch (ExecutionException | InterruptedException e) {
            // REPETIR ??
            e.printStackTrace();
        }

        System.out.println("Begin " + xContext.getXid());

        Book b = store.search(xContext, "one");

        try {
            dor.commit(xContext);
        } catch (InterruptedException | ExecutionException e) {
            // REPETIR ??
            e.printStackTrace();
        }

        /*Cart c = store.newCart();

        c.add(b);

        c.buy();*/
    }
}
