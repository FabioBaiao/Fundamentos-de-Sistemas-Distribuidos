package bank;

import common.Req;

public class AccountGetPaymentHistoryReq extends Req {
	public AccountGetPaymentHistoryReq() {}
	public AccountGetPaymentHistoryReq(int accountId) { super(accountId); }
}
