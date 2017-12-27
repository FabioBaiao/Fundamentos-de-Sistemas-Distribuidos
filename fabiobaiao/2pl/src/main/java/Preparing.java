import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class Preparing implements CatalystSerializable {

    int xid;
    List<Integer> participants;

    public Preparing() {}

    public Preparing(int xid, List<Integer> participants) {
        this.xid = xid;
        this.participants = participants;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
        bufferOutput.writeInt(participants.size());
        for (int participant : participants) {
            bufferOutput.writeInt(participant);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
        int size = bufferInput.readInt();
        participants = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            participants.add(bufferInput.readInt());
        }
    }
}
