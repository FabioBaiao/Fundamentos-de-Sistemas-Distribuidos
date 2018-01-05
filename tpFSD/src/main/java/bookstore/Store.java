package bookstore;

import java.util.SortedSet;

public interface Store {
	Book search(String title);
	Cart newCart(int clientId);
	SortedSet<Order> getOrderHistory(int clientId);
}
