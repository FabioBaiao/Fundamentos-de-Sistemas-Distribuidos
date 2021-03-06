package bookstore;

import common.AbstractReq;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class StoreGetOrderHistoryReq extends AbstractReq {

    private int clientId;

    public StoreGetOrderHistoryReq() {}

    public StoreGetOrderHistoryReq(TransactionContext txCtxt, int storeId, int clientId) {
        super(txCtxt, storeId);
        this.clientId = clientId;
    }

    public int getClientId() { return clientId; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeInt(clientId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.clientId = bufferInput.readInt();
    }
}
