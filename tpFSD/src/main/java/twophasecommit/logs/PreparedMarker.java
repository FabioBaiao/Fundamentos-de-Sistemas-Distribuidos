package twophasecommit.logs;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

public class PreparedMarker implements CatalystSerializable{

    private int xid;
    private List<Object> changes;
    private int client;
    // locks

    public PreparedMarker() {}

    public PreparedMarker(int xid, int client, List<Object> changes) {
        this.xid = xid;
        this.client = client;
        this.changes = changes;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
        bufferOutput.writeInt(client);
        bufferOutput.writeInt(changes.size());
        for (Object o : changes) {
            serializer.writeObject(o, bufferOutput);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
        client = bufferInput.readInt();
        int size = bufferInput.readInt();
        for (int i = 0; i < size; i++) {
            changes.add(serializer.readObject(bufferInput));
        }
    }

    public int getXid() {
        return xid;
    }

    public int getClient() {
        return client;
    }
}
