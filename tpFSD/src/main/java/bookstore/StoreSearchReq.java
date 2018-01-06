package bookstore;

import common.AbstractReq;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class StoreSearchReq extends AbstractReq {
    private String title;

    public StoreSearchReq() {}

    public StoreSearchReq(TransactionContext txCtxt, int storeId, String title) {
        super(txCtxt, storeId);
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
