package bank;

import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetNameRep extends Rep {
	private String name;

	// We can't have both BankGetNameRep(String name) and BankGetNameRep(String error).
	// One solution is merging them into one constructor; given that a bank must have a
	// name, when (name == null) surely an error ocurred.
	public BankGetNameRep(String name, String error) {
		if (name == null) {
			super.ok = false;
			super.error = error;
		} else {
			this.name = name;
		}
	}

    public String getName() { return name; }

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
