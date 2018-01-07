package twophasecommit.logs;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class ResourceMarker implements CatalystSerializable{
    private int participant;
    private int xid;

    ResourceMarker() {}

    public ResourceMarker(int xid, int participant) {
        this.xid = xid;
        this.participant = participant;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(xid);
        bufferOutput.writeInt(participant);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xid = bufferInput.readInt();
        participant = bufferInput.readInt();
    }

    public int getXid() {
        return xid;
    }

    public int getParticipant() {
        return participant;
    }
}
