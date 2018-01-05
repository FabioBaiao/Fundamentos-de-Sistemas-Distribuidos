package bookstore;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class StoreGetOrderHistoryRep extends Rep {

    SortedSet<Order> orderHistory;

    public StoreGetOrderHistoryRep() {}
    
    public StoreGetOrderHistoryRep(SortedSet<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public StoreGetOrderHistoryRep(String error) {
        super(error);
        this.orderHistory = Collections.emptySortedSet();
    }

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
