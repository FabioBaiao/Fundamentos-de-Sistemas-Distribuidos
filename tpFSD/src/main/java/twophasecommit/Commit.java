package twophasecommit;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Commit implements CatalystSerializable{

    private TransactionContext xContext;
    private Object context;

    public Commit() {}

    public Commit(TransactionContext xContext) {
        this.xContext = xContext;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(xContext, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        xContext = serializer.readObject(bufferInput);
    }

    public TransactionContext getContext() {
        return xContext;
    }
}
