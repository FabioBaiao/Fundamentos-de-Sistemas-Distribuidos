package bank;

import common.Req;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetAccountReq extends Req {
	private int accountNo;

	public BankGetAccountReq() {}

	public BankGetAccountReq(int bankId, int accountNo) {
		super(bankId);
		this.accountNo = accountNo;
	}

    public int getAccountNo() { return accountNo; }

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
