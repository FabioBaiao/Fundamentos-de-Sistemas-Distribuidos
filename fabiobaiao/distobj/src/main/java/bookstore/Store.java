package bookstore;

public interface Store {

    Book search(String title);

    Cart newCart();

}
