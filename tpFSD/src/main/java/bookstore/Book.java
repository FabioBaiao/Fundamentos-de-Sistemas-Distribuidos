package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Book implements CatalystSerializable {
    private int isbn;
    private String title, author;
    private double price;

    public Book() {}

    public Book(int isbn, String title, String author, double price) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public int getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || o.getClass() != this.getClass())
            return false;

        Book b = (Book) o;
        return (this.isbn == b.isbn);
    }

    @Override
    public int hashCode() { return 31 * 17 + isbn; } // based on item 9 of Joshua Bloch's Effective Java

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
        bufferOutput.writeString(title);
        bufferOutput.writeString(author);
        bufferOutput.writeDouble(price);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.isbn = bufferInput.readInt();
        this.title = bufferInput.readString();
        this.author = bufferInput.readString();
        this.price = bufferInput.readDouble();
    }
}
