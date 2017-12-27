package bookstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStore implements Store {
    private Map<Integer, Book> books = new HashMap<>();

    LocalStore() {
        books.put(1, new LocalBook(1, "one", "someone"));
        books.put(2, new LocalBook(2, "other", "someother"));
    }

    public Book search(String title) {
        for(Book b: books.values())
            if (b.getTitle().equals(title))
                return b;
        return null;
    }

    public Cart newCart() {
        return new LocalCart();
    }

    class LocalCart implements Cart {
        private List<Book> content;

        LocalCart() {
            this.content = new ArrayList<>();
        }

        public boolean add(Book b) {

            content.add(b);
            return true;
        }

        public boolean buy() {
            content.clear();
            return true;
        }
    }
}
