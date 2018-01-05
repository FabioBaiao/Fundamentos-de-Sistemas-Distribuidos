package bookstore;

import bank.Bank;
import java.util.Set;

public interface Cart {
	boolean add(Book b);
	boolean remove(Book b);
	void clear();
	Set<Book> getContent();
	Order buy(Bank.Account srcAccount, String paymentDescription);
}
