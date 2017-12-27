package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddReq implements CatalystSerializable {
    public int cartId;
    public int bookId;

    public CartAddReq(){}

    public CartAddReq(int bookId, int cartId){
        this.bookId = bookId;
        this.cartId = cartId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(bookId);
        bufferOutput.writeInt(cartId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bookId = bufferInput.readInt();
        cartId = bufferInput.readInt();
    }
}
