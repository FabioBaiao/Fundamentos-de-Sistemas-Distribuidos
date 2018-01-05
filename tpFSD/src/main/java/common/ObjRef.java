package common;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class ObjRef implements CatalystSerializable {
    int cliqueId;
    int objectId;
    String cls;

    public ObjRef() {}

    public ObjRef(int cliqueId, int id, String cls) {
        this.cliqueId = cliqueId;
        this.objectId = id;
        this.cls = cls;
    }

    public int getCliqueId() { return cliqueId; }
    public int getObjectId() { return objectId; }
    public String getCls() { return cls; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(objectId);
        serializer.writeObject(cliqueId, bufferOutput);
        bufferOutput.writeString(cls);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        objectId = bufferInput.readInt();
        cliqueId = serializer.readObject(bufferInput);
        cls = bufferInput.readString();
    }
}
