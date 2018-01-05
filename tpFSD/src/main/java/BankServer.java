import bank.Bank;
import bank.LocalBank;
import bookstore.Book;
import bookstore.LocalStore;
import bookstore.Store;
import common.Common;
import common.DistributedObjectsRuntime;

import java.util.HashMap;
import java.util.Map;

public class BankServer {
    public static void main(String[] args) {

        int id = Common.BANK_SERVER_ID;

        DistributedObjectsRuntime dor = new DistributedObjectsRuntime(id);

        dor.init();

        Map<Integer, Bank.Account> accounts = new HashMap<>();

        accounts.put(Common.STORE_ACCOUNT_ID, new LocalBank.LocalAccount(Common.STORE_ACCOUNT_ID));

        Bank bank = new LocalBank("CGD", accounts);

        dor.objExport(bank);

        System.out.println("Finished");
    }
}
