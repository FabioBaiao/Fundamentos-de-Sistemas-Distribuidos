import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public abstract class Req implements CatalystSerializable {
	public int objId;

	protected Req() {}
	protected Req(int objId) { this.objId = objId; }

	@Override
	public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		bufferOutput.writeInt(objId);
	}

	@Override
	public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		this.objId = bufferInput.readInt();
	}
}
