package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountPayRep extends Rep {
	public AccountPayRep() {}
	public AccountPayRep(String error) { super(error); }
}
