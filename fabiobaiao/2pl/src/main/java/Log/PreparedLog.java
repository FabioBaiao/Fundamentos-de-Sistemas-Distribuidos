package Log;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class PreparedLog implements CatalystSerializable{

    public int xid;
    List<Object> changes;
    public int client;
    // locks

    public PreparedLog() {}

    public PreparedLog(int xid, int client, List<Object> changes) {
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
}
