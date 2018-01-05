package bank;

import common.ObjRef;
import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetAccountRep extends Rep {
	private ObjRef accountRef;

	public BankGetAccountRep() {}
	public BankGetAccountRep(ObjRef accountRef) { this.accountRef = accountRef; }
	public BankGetAccountRep(String error) { super(error); }

    public ObjRef getAccountRef() { return accountRef; }

    @Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		serializer.writeObject(accountRef, bufferOutput);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.accountRef = serializer.readObject(bufferInput);
	}
}
