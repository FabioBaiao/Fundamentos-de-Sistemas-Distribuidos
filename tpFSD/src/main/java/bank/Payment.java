package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Payment implements CatalystSerializable, Comparable<Payment> {

	private LocalDateTime dateTime;
	private double amount;
	private String description;

	public Payment() {}

	public Payment(double amount) {
		this(amount, null);
	}

	public Payment(double amount, String description) {
		this.dateTime = LocalDateTime.now();
		this.amount = amount;
		this.description = description;
	}

	public LocalDateTime getDateTime() { return dateTime; }
	public double getAmount() { return amount; }
	public String getDescription() { return description; }

	// Decreasing order by LocalDateTime
	@Override
	public int compareTo(Payment p) {
		return p.dateTime.compareTo(this.dateTime);
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);

		bufferOutput.writeLong(date.getTime());
		bufferOutput.writeDouble(amount);
		bufferOutput.writeString(description);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		Instant instant = Instant.ofEpochMilli(bufferInput.readLong());

		this.dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		this.amount = bufferInput.readDouble();
		this.description = bufferInput.readString();
	}
}
