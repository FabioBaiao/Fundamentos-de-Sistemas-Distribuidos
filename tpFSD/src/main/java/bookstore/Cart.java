package bookstore;

import bank.Bank;
import twophasecommit.TransactionContext;

import java.util.Set;

public interface Cart {
	boolean add(TransactionContext txCtxt, Book b);
	boolean remove(TransactionContext txCtxt, Book b);
	void clear(TransactionContext txCtxt);
	Set<Book> getContent(TransactionContext txCtxt);
	Order buy(TransactionContext txCtxt, Bank.Account srcAccount, String paymentDescription);
}
