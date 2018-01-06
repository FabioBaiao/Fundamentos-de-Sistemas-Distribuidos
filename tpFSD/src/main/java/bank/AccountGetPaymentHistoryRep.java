package bank;

import common.AbstractRep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class AccountGetPaymentHistoryRep extends AbstractRep {
	private SortedSet<Payment> paymentHistory;

	public AccountGetPaymentHistoryRep() {}

	public AccountGetPaymentHistoryRep(SortedSet<Payment> paymentHistory) {
		this.paymentHistory = (paymentHistory == null) ? Collections.emptySortedSet() : paymentHistory;
	}

	public AccountGetPaymentHistoryRep(String error) {
		super(error);
		this.paymentHistory = Collections.emptySortedSet();
	}

	public SortedSet<Payment> getPaymentHistory() { return paymentHistory; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);

		bufferOutput.writeInt(paymentHistory.size());
		for (Payment p : paymentHistory) {
			serializer.writeObject(p, bufferOutput);
		}
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);

		final int size = bufferInput.readInt();
		this.paymentHistory = new TreeSet<>();
		for (int i = 0; i < size; i++) {
			this.paymentHistory.add(serializer.readObject(bufferInput));
		}
	}
}
