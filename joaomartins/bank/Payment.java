package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

// import java.time.LocalDateTime;

public class Payment implements CatalystSerializable {
	private double amount;
	private String description;
//	private final LocalDateTime dateTime;

	public Payment() {}

	public Payment(double amount) {
		this(amount, null);
	}

	public Payment(double amount, String description) {
		this.amount = amount;
		this.description = description;
//		this.dateTime = LocalDateTime.now();
	}

	public double getAmmount() { return amount; }
	public String getDescription() { return description; }
//	public LocalDateTime getDateTime() { return dateTime; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeDouble(amount);
		bufferOutput.writeString(description);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.amount = bufferInput.readDouble();
		this.description = bufferInput.readString();
	}
}
