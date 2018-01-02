package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankPayFromRep implements CatalystSerializable {
	private boolean ok;
	private String error; // if we define that (error == null) means no error, BankPayFromRep.ok is unnecessary

	public BankPayFromRep() {}

	public BankPayFromRep(boolean ok) { this(ok, null); }

	public BankPayFromRep(boolean ok, String error) {
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
