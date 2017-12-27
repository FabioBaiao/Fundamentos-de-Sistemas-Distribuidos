package bookstore;

public class ClientV2 {
    public static void main(String[] args) throws Exception {
        Store s = new RemoteStore();
        Book book1 = s.search("one");
        System.out.println("isbn = "+book1.getIsbn());
        Cart cart = s.newCart();
        cart.addBook(book1);
    }
}