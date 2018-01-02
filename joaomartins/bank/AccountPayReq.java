package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountPayReq implements CatalystSerializable {
	private int accountId;
	private double amount;
	private String description;

	public AccountPayReq() {}

	public AccountPayReq(int accountId, double amount) {
		this(accountId, amount, null);
	}

	public AccountPayReq(int accountId, double amount, String description) {
		this.accountId = accountId;
		this.amount = amount;
	}

	public int getAccountId() { return accountId; }
	public double getAmmount() { return amount; }
	public String getDescription() { return description; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(accountId);
		bufferOutput.writeDouble(amount);
		bufferOutput.writeString(description);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.accountId = bufferInput.readInt();
		this.amount = bufferInput.readDouble();
		this.description = bufferInput.readString();
	}
}
