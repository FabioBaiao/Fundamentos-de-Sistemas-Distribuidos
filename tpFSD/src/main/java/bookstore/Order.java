package bookstore;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Order implements CatalystSerializable, Comparable<Order> {
	private LocalDateTime dateTime;
	private Set<Book> books;

	public Order() {}
	
	public Order(Set<Book> books) {
		this.dateTime = LocalDateTime.now();
		this.books = books;
	}

	public LocalDateTime getDateTime() { return dateTime; }
	public Set<Book> getBooks() { return books; }

	// Decreasing order by LocalDateTime
	@Override
	public int compareTo(Order o) {
		return o.dateTime.compareTo(this.dateTime);
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);

		bufferOutput.writeLong(date.getTime());
		bufferOutput.writeInt(books.size());
		for (Book b : books) {
			serializer.writeObject(b, bufferOutput);
		}
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		Instant instant = Instant.ofEpochMilli(bufferInput.readLong());
		this.dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

		final int size = bufferInput.readInt();
		this.books = new HashSet<>(size);
		for (int i = 0; i < size; i++) {
			this.books.add(serializer.readObject(bufferInput));
		}
	}
	/*
		public static class OrderLine {
			private Book book;
			private int qty;
		}
	*/
}
