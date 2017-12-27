package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class StoreSearchReq implements CatalystSerializable {
    int storeid;
    String title;

    public StoreSearchReq() {}

    StoreSearchReq(int storeid, String title) {
        this.storeid = storeid;
        this.title = title;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeid);
        bufferOutput.writeString(title);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeid = bufferInput.readInt();
        title = bufferInput.readString();
    }
}
