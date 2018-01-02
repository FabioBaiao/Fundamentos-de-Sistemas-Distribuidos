package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

// TODO: Consider creating an abstract reply class with fields "ok" and "error".
public class AccountPayRep implements CatalystSerializable {
	private boolean ok;
	private String error;

	public AccountPayRep() {}

	public AccountPayRep(boolean ok) { this(ok, null); }

	public AccountPayRep(boolean ok, String error) {
		this.ok = ok;
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
