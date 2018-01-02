package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountGetPaymentHistoryReq implements CatalystSerializable {
	private int accountId;

	public AccountGetPaymentHistoryReq() {}
	public AccountGetPaymentHistoryReq(int accountId) { this.accountId = accountId; }

	public int getAccountId() { return accountId; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(accountId);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.accountId = bufferInput.readInt();
	}
}
