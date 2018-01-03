package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountGetBalanceReq extends Req {
	public AccountGetBalanceReq() {}
	public AccountGetBalanceReq(int accountId) { super(accountId); }
}
