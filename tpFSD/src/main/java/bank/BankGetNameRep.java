package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetNameRep extends Rep {
	String name;

	// We can't have both BankGetNameRep(String name) and BankGetNameRep(String error).
	// One solution is merging them into one constructor; given that a bank must have a
	// name, (name == null) means that an error ocurred.
	public BankGetNameRep(String name, String error) {
		if (name == null) {
			super.ok = false;
			super.error = error;
		} else {
			this.name = name;
		}
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeString(name);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.name = bufferInput.readString();
	}
}
