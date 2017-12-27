package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Book implements CatalystSerializable {
    public String title;
    public String author;
    public int isbn;

    public Book(String title, String author, int isbn){
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    public int getIsbn(){
        return isbn;
    }
    public String getTitle(){
        return title;
    }
    public String getAuthor(){
        return author;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
        bufferOutput.writeString(title);
        bufferOutput.writeString(author);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.isbn = bufferInput.readInt();
        this.title = bufferInput.readString();
        this.author = bufferInput.readString();
    }
}
