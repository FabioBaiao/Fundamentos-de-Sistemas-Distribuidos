package bank;

import common.Rep;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AccountGetNoRep extends Rep {
	private int no;

	public AccountGetNoRep() {}
	public AccountGetNoRep(int no) { this.no = no; }
	public AccountGetNoRep(String error) { super(error); }

	public int getNo() { return no; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeInt(no);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.no = bufferInput.readInt();
	}
}
