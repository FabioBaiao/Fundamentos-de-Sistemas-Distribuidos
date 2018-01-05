package bookstore;

import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class StoreSearchRep extends Rep {

    private Book book;

    public StoreSearchRep() {}
    public StoreSearchRep(Book book) { this.book = book; }
    public StoreSearchRep(String error) { super(error); }

    public Book getBook() { return book; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        serializer.writeObject(book, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.book = serializer.readObject(bufferInput);
    }
}
