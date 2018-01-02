package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankGetPaymentHistoryRep implements CatalystSerializable {
	private List<Payment> paymentHistory;
	private String error;

	public BankGetPaymentHistoryRep() {}

	public BankGetPaymentHistoryRep(List<Payment> paymentHistory) {
		this.paymentHistory = paymentHistory;
		this.error = null;
	}

	public BankGetPaymentHistoryRep(String error) {
		this.paymentHistory = Collections.emptyList();
		this.error = error;
	}

	public List<Payment> getPaymentHistory() { return paymentHistory; }
	public String getError() { return error; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(paymentHistory.size());
		for (Payment p : paymentHistory)
			serializer.writeObject(p, bufferOutput);

		bufferOutput.writeString(error);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		final int size = bufferInput.readInt();

		this.paymentHistory = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			this.paymentHistory.add(serializer.readObject(bufferInput));

		this.error = bufferInput.readString();
	}
}
