package bookstore;

import common.ObjRef;
import common.Req;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class CartBuyReq extends Req {
	private ObjRef srcAccountRef;
	private String description;

	public CartBuyReq() {}

	public CartBuyReq(TransactionContext txCtxt, int cartId, ObjRef srcAccountRef, String description) {
		super(txCtxt, cartId);
		this.srcAccountRef = srcAccountRef;
		this.description = description;
	}

    public ObjRef getSrcAccountRef() { return srcAccountRef; }
    public String getDescription() { return description; }

    @Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		serializer.writeObject(srcAccountRef, bufferOutput);
		bufferOutput.writeString(description);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.srcAccountRef = serializer.readObject(bufferInput);
		this.description = bufferInput.readString();
	}
}
