package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountGetPaymentHistoryReq extends Req {
	public AccountGetPaymentHistoryReq() {}
	public AccountGetPaymentHistoryReq(int accountId) { super(accountId); }
}
