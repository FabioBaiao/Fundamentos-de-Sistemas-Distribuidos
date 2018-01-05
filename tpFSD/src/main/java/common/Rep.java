package common;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public abstract class Rep implements CatalystSerializable {
	protected boolean ok;
	protected String error;

	protected Rep() { this.ok = true; }

	// If an error is provided, ok is false (if the error is null, an unspecified error ocurred)
	protected Rep(String error) {
		this.ok = false;
		this.error = error;
	}

	public boolean isOk() { return ok; }
	public String getError() { return error; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeBoolean(ok);
		bufferOutput.writeString(error);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.ok = bufferInput.readBoolean();
		this.error = bufferInput.readString();
	}
}
