package bookstore;

import java.util.HashMap;
import java.util.Map;

public class LocalStore implements Store{
    public Map<Integer,Book> books ;

    public LocalStore(){
        books = new HashMap<>();
        books.put(1, new Book("one", "author", 1));
        books.put(2, new Book("two", "author", 2));
    }

    @Override
    public Book get(int isbn) {
        return books.get(isbn);
    }

    @Override
    public Book search(String title) {
        for(Book b: books.values()){
            if(b.getTitle().equals(title))
                return b;
        }
        return null;
    }

    @Override
    public Cart newCart() throws Exception {
        return null;
    }
}
