package Communication;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CommitComm implements CatalystSerializable{

    public int xid;

    CommitComm() {}

    public CommitComm(int xid) {
        this.xid = xid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
    }
}
