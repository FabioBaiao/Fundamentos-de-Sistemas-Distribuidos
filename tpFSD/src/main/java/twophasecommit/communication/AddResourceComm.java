package twophasecommit.communication;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AddResourceComm implements CatalystSerializable{
    private Integer xid;
    private int client;

    public AddResourceComm() {}

    public AddResourceComm(int xid, int client) {
        this.xid = xid;
        this.client = client;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
        bufferOutput.writeInt(client);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
        client = bufferInput.readInt();
    }

    public Integer getXid() {
        return xid;
    }

    public int getClient() {
        return client;
    }
}
