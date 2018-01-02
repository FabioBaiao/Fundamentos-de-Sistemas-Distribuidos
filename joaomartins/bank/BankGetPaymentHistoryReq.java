package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankGetPaymentHistoryReq implements CatalystSerializable {
	private int bankId;
	private int accountNo;

	public BankGetPaymentHistoryReq() {}

	public BankGetPaymentHistoryReq(int bankId, int accountNo) {
		this.bankId = bankId;
		this.accountNo = accountNo;
	}

	public int getBankId() { return bankId; }
	public int getAccountNo() { return accountNo; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(bankId);
		bufferOutput.writeInt(accountNo);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.bankId = bufferInput.readInt();
		this.accountNo = bufferInput.readInt();
	}
}
