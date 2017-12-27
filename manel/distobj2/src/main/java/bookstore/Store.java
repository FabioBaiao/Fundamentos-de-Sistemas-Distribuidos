package bookstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Store {
    //public Map<Integer,Book> books = new HashMap<>();

    public Book get(int isbn);
    public Book search(String title) throws Exception;
    public Cart newCart() throws Exception;

    /*
    public class Cart {
        private List<Book> content;

        public void add(Book b) {
            content.add(b);
        }

        public boolean buy() {
            content.clear();
            return true;
        }
    }*/
}
