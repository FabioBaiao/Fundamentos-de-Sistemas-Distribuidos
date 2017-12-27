package bookstore;

public interface Cart {
    public void addBook(Book b) throws Exception;
    public boolean buy();
}
