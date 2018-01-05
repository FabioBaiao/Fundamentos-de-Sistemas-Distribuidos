package bank;

import common.Req;
import twophasecommit.TransactionContext;

public class AccountGetBalanceReq extends Req {
	public AccountGetBalanceReq() {}
	public AccountGetBalanceReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
