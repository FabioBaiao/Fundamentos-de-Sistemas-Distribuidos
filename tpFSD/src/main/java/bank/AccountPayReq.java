package bank;

import common.ObjRef;
import common.AbstractReq;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class AccountPayReq extends AbstractReq {
	private double amount;
	private String description;

    private ObjRef dstRef;

	public AccountPayReq() {}

	public AccountPayReq(TransactionContext txCtxt, int accountId, double amount, String description, ObjRef dstRef) {
		super(txCtxt, accountId);
		this.amount = amount;
		this.description = description;
		this.dstRef = dstRef;
	}

    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public ObjRef getDstRef() { return dstRef; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		bufferOutput.writeDouble(amount);
		bufferOutput.writeString(description);
		serializer.writeObject(dstRef, bufferOutput);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.amount = bufferInput.readDouble();
		this.description = bufferInput.readString();
		this.dstRef = serializer.readObject(bufferInput);
	}
}
