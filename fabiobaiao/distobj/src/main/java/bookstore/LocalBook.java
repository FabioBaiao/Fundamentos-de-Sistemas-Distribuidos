package bookstore;

public class LocalBook implements Book {
    LocalBook(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    private int isbn;
    private String title, author;
}
