package bookstore;

import common.AbstractReq;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import twophasecommit.TransactionContext;

public class CartRemoveReq extends AbstractReq {
	Book book;

	public CartRemoveReq() {}

	public CartRemoveReq(TransactionContext txCtxt, int cartId, Book book) {
		super(txCtxt, cartId);
		this.book = book;
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		serializer.writeObject(book, bufferOutput);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		this.book = serializer.readObject(bufferInput);
	}

	public Book getBook() {
		return book;
	}
}
