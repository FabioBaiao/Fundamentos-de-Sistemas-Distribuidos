package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class CartBuyReq extends Req {
	ObjRef srcAccountRef;
	String description;

	public CartBuyReq() {}

	public CartBuyReq(int cartId, ObjRef srcAccountRef, String description) {
		super(cartId);
		this.srcAccountRef = srcAccountRef;
		this.description = description;
	}

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
