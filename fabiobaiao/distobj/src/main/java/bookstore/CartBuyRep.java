package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartBuyRep extends BaseRep implements CatalystSerializable {
    boolean result;

    CartBuyRep() {}

    CartBuyRep(boolean result) {
        this.result = result;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(result);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        result = bufferInput.readBoolean();
    }
}
