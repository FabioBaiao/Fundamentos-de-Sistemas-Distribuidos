package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookIsbnRep implements CatalystSerializable{
    public int isbn;

    public BookIsbnRep(){}

    public BookIsbnRep(int isbn){
        this.isbn = isbn;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.isbn = bufferInput.readInt();
    }
}
