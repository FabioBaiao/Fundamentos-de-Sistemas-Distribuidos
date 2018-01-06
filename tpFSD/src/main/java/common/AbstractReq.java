package common;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public abstract class AbstractReq implements CatalystSerializable {
    private TransactionContext txCtxt;
	private int objId;

	protected AbstractReq() {}

	protected AbstractReq(TransactionContext txCtxt, int objId) {
	    this.txCtxt = txCtxt;
	    this.objId = objId;
	}

    public TransactionContext getTransactionContext() { return txCtxt; }
    public int getObjId() { return objId; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
	    serializer.writeObject(txCtxt, bufferOutput);
		bufferOutput.writeInt(objId);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
	    this.txCtxt = serializer.readObject(bufferInput);
		this.objId = bufferInput.readInt();
	}
}
