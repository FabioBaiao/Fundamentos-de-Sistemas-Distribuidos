package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankPayFromReq implements CatalystSerializable {
	private int id;
	private int accountNo;
	private double amount;

	public BankPayFromReq() {}

	public BankPayFromReq(int id, int accountNo, double amount) {
		this.id = id;
		this.accountNo = accountNo;
		this.amount = amount;
	}

	public int getId() { return id; }
	public int getAccountNo() { return accountNo; }
	public double getAmmount() { return amount; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(id);
		bufferOutput.writeInt(accountNo);
		bufferOutput.writeDouble(amount);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.id = bufferInput.readInt();
		this.accountNo = bufferInput.readInt();
		this.amount = bufferInput.readDouble();
	}
}
