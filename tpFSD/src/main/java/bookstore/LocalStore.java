package bookstore;

import bank.Bank;

import java.util.*;

// Simplification: There's unlimited stock of each book.
public class LocalStore implements Store {
    private Bank.Account storeAccount;
    private Map<Integer, Book> books; // key: isbn
    private Map<Integer, SortedSet<Order>> orderHistory; // key: clientId

    public LocalStore(Bank.Account storeAccount, Map<Integer, Book> books) {
        Objects.requireNonNull(storeAccount);

        this.storeAccount = storeAccount;
        this.books = new HashMap<>(books);
        this.orderHistory = new HashMap<>();
        /*
                books.put(1, new Book(1, "one", "someone", 10.0));
                books.put(2, new Book(2, "other", "someother", 12.5));
        */
    }

    @Override
    public Book search(String title) {
        for (Book b : books.values())
            if (b.getTitle().equals(title))
                return b;
        return null;
    }

    @Override
    public Cart newCart(int clientId) {
        return new LocalCart(clientId);
    }

    @Override
    public SortedSet<Order> getOrderHistory(int clientId) {
        return orderHistory.get(clientId);
    }

    // Simplification: each cart has at most one copy of a given book
    public class LocalCart implements Cart {
        private int clientId;
        private Set<Book> content;

        public LocalCart(int clientId) {
            this.clientId = clientId;
            this.content = new HashSet<>();
        }

        @Override
        public boolean add(Book b) {
            return content.add(b);
        }

        @Override
        public boolean remove(Book b) {
            return content.remove(b);
        }

        @Override
        public void clear() {
            content.clear();
        }

        @Override
        public Set<Book> getContent() {
            return content;
        }

        @Override
        public Order buy(Bank.Account srcAccount, String paymentDescription) {
            double total = 0.0;

            for (Book b : content)
                total += b.getPrice();

            srcAccount.pay(total, paymentDescription, LocalStore.this.storeAccount);

            SortedSet<Order> clientOrders = LocalStore.this.orderHistory.get(clientId);
            if (clientOrders == null) { // the client has no previous orders
                clientOrders = new TreeSet<>();
                LocalStore.this.orderHistory.put(clientId, clientOrders);
            }
            Order o = new Order(content);
            clientOrders.add(o);
            
            content = new HashSet<>();
            return o;
        }
    }
}
