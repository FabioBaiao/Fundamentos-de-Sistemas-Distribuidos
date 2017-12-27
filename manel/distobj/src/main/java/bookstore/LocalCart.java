package bookstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalCart implements Cart {
    private int id;
    private List<Book> books;

    public LocalCart(int id){
        this.id = id;
        this.books = new ArrayList<>();
    }

    @Override
    public void addBook(Book b) {
        books.add(b);
    }

    @Override
    public boolean buy() {
        return false;
    }
}
