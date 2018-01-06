package bookstore;

import common.AbstractRep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class StoreGetOrderHistoryRep extends AbstractRep {

    private SortedSet<Order> orderHistory;

    public StoreGetOrderHistoryRep() {}
    
    public StoreGetOrderHistoryRep(SortedSet<Order> orderHistory) {
        this.orderHistory = (orderHistory == null) ? Collections.emptySortedSet() : orderHistory;
    }

    public StoreGetOrderHistoryRep(String error) {
        super(error);
        this.orderHistory = Collections.emptySortedSet();
    }

    public SortedSet<Order> getOrderHistory() { return orderHistory; }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        
        final int size = orderHistory.size();
        bufferOutput.writeInt(size);
        for (Order o : orderHistory) {
            serializer.writeObject(o, bufferOutput);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        
        final int size = bufferInput.readInt();
        this.orderHistory = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            this.orderHistory.add(serializer.readObject(bufferInput));
        }
    }
}
