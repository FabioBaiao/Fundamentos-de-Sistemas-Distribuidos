package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountGetPaymentHistoryRep extends Rep {
	List<Payment> paymentHistory;

	public AccountGetPaymentHistoryRep() {}

	public AccountGetPaymentHistoryRep(List<Payment> paymentHistory) {
		this.paymentHistory = paymentHistory;
	}

	public AccountGetPaymentHistoryRep(String error) {
		super(error);
		this.paymentHistory = Collections.emptyList();
	}

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
		this.paymentHistory = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			this.paymentHistory.add(serializer.readObject(bufferInput));
		}
	}
}
