package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookAuthorRep implements CatalystSerializable{
    public String author;

    public BookAuthorRep(){}

    public BookAuthorRep(String author){
        this.author = author;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(author);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.author = bufferInput.readString();
    }
}
