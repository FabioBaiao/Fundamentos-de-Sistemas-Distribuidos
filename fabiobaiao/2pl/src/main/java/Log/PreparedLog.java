package Log;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class PreparedLog implements CatalystSerializable{

    int xid;
    List<Object> changes;

    public PreparedLog() {}

    public PreparedLog(int xid, List<Object> changes) {
        this.xid = xid;
        this.changes = changes;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
        bufferOutput.writeInt(changes.size());
        for (Object o : changes) {
            serializer.writeObject(o, bufferOutput);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
        int size = bufferInput.readInt();
        for (int i = 0; i < size; i++) {
            changes.add(serializer.readObject(bufferInput));
        }
    }
}
