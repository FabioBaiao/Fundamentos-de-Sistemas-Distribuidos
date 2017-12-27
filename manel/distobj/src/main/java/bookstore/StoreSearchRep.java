package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class StoreSearchRep implements CatalystSerializable {
    public int id;
    public Address addr;
    public String type;

    public StoreSearchRep() {}

    public StoreSearchRep(int id, Address addr, String type) {
        this.id = id;
        this.addr = addr;
        this.type = type;
    }

    public ObjRef getReference(){
        return new ObjRef(addr, id, type);
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
        bufferOutput.writeString(addr.toString());
        bufferOutput.writeString(type);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.id = bufferInput.readInt();
        this.addr = new Address(bufferInput.readString());
        this.type = bufferInput.readString();
    }
}
