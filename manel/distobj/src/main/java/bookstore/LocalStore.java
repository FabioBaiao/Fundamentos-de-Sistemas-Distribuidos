package bookstore;

import java.util.HashMap;
import java.util.Map;

public class LocalStore implements Store{
    public Map<Integer,LocalBook> books ;

    public LocalStore(){
        books = new HashMap<>();
        books.put(1, new LocalBook(1, "one", "cenas"));
        books.put(2, new LocalBook(2, "two", "cenas"));
    }

    @Override
    public Book get(int isbn) {
        return books.get(isbn);
    }

    @Override
    public Book search(String title) {
        for(LocalBook b: books.values()){
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
