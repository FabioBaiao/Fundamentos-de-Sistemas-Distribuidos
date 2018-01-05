package bank;

import common.Req;

public class AccountGetBalanceReq extends Req {
	public AccountGetBalanceReq() {}
	public AccountGetBalanceReq(int accountId) { super(accountId); }
}
