package bookstore;

import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class CartRemoveRep extends Rep {
	boolean removed;

	public CartRemoveRep() {}
	public CartRemoveRep(boolean removed) { this.removed = removed; }
	public CartRemoveRep(String error) { super(error); }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeBoolean(removed);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.removed = bufferInput.readBoolean();
	}
}
