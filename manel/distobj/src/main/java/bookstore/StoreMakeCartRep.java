package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class StoreMakeCartRep implements CatalystSerializable {
    private Address addr;
    private int id;
    private String type;

    public StoreMakeCartRep(){}

    public StoreMakeCartRep(Address addr, int id, String type){
        this.addr = addr;
        this.id = id;
        this.type = type;
    }

    public ObjRef getReference(){
        return new ObjRef(addr, id, type);
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(addr.toString());
        bufferOutput.writeInt(id);
        bufferOutput.writeString(type);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.addr = new Address(bufferInput.readString());
        this.id = bufferInput.readInt();
        this.type = bufferInput.readString();
    }
}
