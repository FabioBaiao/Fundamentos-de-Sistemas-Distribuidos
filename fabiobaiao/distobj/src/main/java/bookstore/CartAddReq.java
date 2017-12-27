package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddReq extends BaseReq implements CatalystSerializable {
    int cartid;
    ObjRef bookRef;

    public CartAddReq() {}

    CartAddReq(int cartid, ObjRef ref) {
        this.cartid = cartid;
        this.bookRef = ref;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartid);
        serializer.writeObject(bookRef, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartid = bufferInput.readInt();
        bookRef = serializer.readObject(bufferInput);
    }
}
