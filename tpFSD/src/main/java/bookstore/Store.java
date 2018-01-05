package bookstore;

import twophasecommit.TransactionContext;

import java.util.SortedSet;

public interface Store {
	Book search(TransactionContext txCtxt, String title);
	Cart newCart(TransactionContext txCtxt, int clientId);
	SortedSet<Order> getOrderHistory(TransactionContext txCtxt, int clientId);
}
