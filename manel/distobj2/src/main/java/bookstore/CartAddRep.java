package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddRep implements CatalystSerializable{
    public int returnCode;
    public String message;

    public CartAddRep(){}

    public CartAddRep(int code){
        this.returnCode = code;
        this.message = "";
    }

    public CartAddRep(int code, String msg){
        this.returnCode = code;
        this.message = msg;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(returnCode);
        bufferOutput.writeString(message);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.returnCode = bufferInput.readInt();
        this.message = bufferInput.readString();
    }
}
