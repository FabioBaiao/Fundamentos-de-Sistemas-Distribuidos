package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class ObjRef implements CatalystSerializable {
    Address address;
    int id;
    String cls;

    public ObjRef() {}

    ObjRef(Address address, int id, String cls) {
        this.address = address;
        this.id = id;
        this.cls = cls;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
        serializer.writeObject(address, bufferOutput);
        bufferOutput.writeString(cls);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        id = bufferInput.readInt();
        address = serializer.readObject(bufferInput);
        cls = bufferInput.readString();
    }
}
