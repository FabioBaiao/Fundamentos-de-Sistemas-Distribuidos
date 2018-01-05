package bookstore;

import common.Req;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class StoreSearchReq extends Req {
    private String title;

    public StoreSearchReq() {}

    public StoreSearchReq(int storeId, String title) {
        super(storeId);
        this.title = title;
    }

    public String getTitle() { return title; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeString(title);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.title = bufferInput.readString();
    }
}
