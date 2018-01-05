package bookstore;

import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddRep extends Rep {
	private boolean added;

	public CartAddRep() {}
	public CartAddRep(boolean added) { this.added = added; }
	public CartAddRep(String error) { super(error); }

	public boolean getAdded() { return added; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeBoolean(added);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.added = bufferInput.readBoolean();
	}
}
