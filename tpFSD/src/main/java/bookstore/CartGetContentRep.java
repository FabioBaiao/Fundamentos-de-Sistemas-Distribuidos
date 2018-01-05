package bookstore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.Rep;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class CartGetContentRep extends Rep {
	Set<Book> content;

	public CartGetContentRep() {}

	public CartGetContentRep(Set<Book> content) {
		this.content = content;
	}

	public CartGetContentRep(String error) {
		super(error);
		this.content = Collections.emptySet();
	}

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		super.writeObject(bufferOutput, serializer);
		final int size = content.size();

		bufferOutput.writeInt(size);
		for (Book b : content) {
			serializer.writeObject(b, bufferOutput);
		}
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		super.readObject(bufferInput, serializer);
		final int size = bufferInput.readInt();

		this.content = new HashSet<>((int) (size / 0.75f) + 1); // avoid rehashing
		for (int i = 0; i < size; i++) {
			content.add(serializer.readObject(bufferInput));
		}
	}
}
