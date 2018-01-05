package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class StoreMakeCartRep extends Rep {

    ObjRef ref;

    public StoreMakeCartRep() {}
    public StoreMakeCartRep(ObjRef ref) { this.ref = ref; }
    public StoreMakeCartRep(String error) { super(error); }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        serializer.writeObject(ref, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.ref = serializer.readObject(bufferInput);
    }
}
