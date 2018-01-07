package twophasecommit.logs;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;


public class BeginMarker implements CatalystSerializable{
    private int xid;
    private int client;

    public BeginMarker() {}

    public BeginMarker(int xid, int client) {
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

    public int getXid() {
        return xid;
    }

    public int getClient() {
        return client;
    }
}
