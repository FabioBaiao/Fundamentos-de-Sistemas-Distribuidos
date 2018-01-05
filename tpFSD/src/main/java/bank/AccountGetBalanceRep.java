package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AccountGetBalanceRep extends Rep {
	double balance;

	public AccountGetBalanceRep() {}
	public AccountGetBalanceRep(double balance) { this.balance = balance; }
	public AccountGetBalanceRep(String error) { super(error); }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeDouble(balance);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.balance = bufferInput.readDouble();
	}
}
