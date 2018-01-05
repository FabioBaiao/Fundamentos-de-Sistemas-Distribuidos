package bank;

import common.Req;
import twophasecommit.TransactionContext;

public class AccountGetNoReq extends Req {
	public AccountGetNoReq() {}
	public AccountGetNoReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
