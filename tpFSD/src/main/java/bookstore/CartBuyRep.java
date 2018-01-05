package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class CartBuyRep extends Rep {
	Order order;

	public CartBuyRep() {}
	public CartBuyRep(Order order) { this.order = order; }
	public CartBuyRep(String error) { super(error); }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		serializer.writeObject(order, bufferOutput);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.order = serializer.readObject(bufferInput);
	}
}
