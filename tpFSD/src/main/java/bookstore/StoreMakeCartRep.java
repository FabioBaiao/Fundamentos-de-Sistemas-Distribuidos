package bookstore;

import common.ObjRef;
import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class StoreMakeCartRep extends Rep {

    private ObjRef cartRef;

    public StoreMakeCartRep() {}
    public StoreMakeCartRep(ObjRef cartRef) { this.cartRef = cartRef; }
    public StoreMakeCartRep(String error) { super(error); }

    public ObjRef getCartRef() { return cartRef; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        serializer.writeObject(cartRef, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.cartRef = serializer.readObject(bufferInput);
    }
}
