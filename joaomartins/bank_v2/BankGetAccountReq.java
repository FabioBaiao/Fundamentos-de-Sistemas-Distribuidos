package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetAccountReq extends Req {
	int accountNo;

	public BankGetAccountReq() {}

	public BankGetAccountReq(int bankId, int accountNo) {
		super(bankId);
		this.accountNo = accountNo;
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeInt(accountNo);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.accountNo = bufferInput.readInt();
	}
}
