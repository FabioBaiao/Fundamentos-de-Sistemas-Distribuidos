package bank;

import common.AbstractReq;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class AccountCreditReq extends AbstractReq {

    private double amount;

	public AccountCreditReq() {}
	
	public AccountCreditReq(TransactionContext txCtxt, int accountId, double amount) {
		super(txCtxt, accountId);
		this.amount = amount;
	}

    public double getAmount() { return amount; }

    @Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeDouble(amount);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.amount = bufferInput.readDouble();
	}
}
