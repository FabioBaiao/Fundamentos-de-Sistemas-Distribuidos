import bank.Bank;
import bookstore.Book;
import bookstore.LocalStore;
import bookstore.Store;
import common.Common;
import common.DistributedObjectsRuntime;
import common.ObjRef;
import twophasecommit.TransactionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StoreServer {
    public static void main(String[] args) {

        int id = Common.STORE_SERVER_ID;

        DistributedObjectsRuntime dor = new DistributedObjectsRuntime(id);

        dor.init();

        Bank bank = (Bank) dor.objImport(new ObjRef(Common.BANK_SERVER_ID, Common.BANK_ID, "Bank"));

        TransactionContext xContext = null;
        try {
            xContext = dor.begin();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Begin " + xContext.getXid());

        Bank.Account account = null;

        while (account == null){
            try {
                account = bank.getAccount(xContext, Common.STORE_ACCOUNT_ID);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        try {
            dor.commit(xContext);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        Map<Integer, Book> books = new HashMap<>();

        books.put(1, new Book(1, "one", "someone", 10.0));
        books.put(2, new Book(2, "other", "someother", 12.5));

        Store store = new LocalStore(account, books);

        dor.objExport(store);

        System.out.println("Finished");
    }
}
