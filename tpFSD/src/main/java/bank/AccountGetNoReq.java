package bank;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class AccountGetNoReq extends AbstractReq {
	public AccountGetNoReq() {}
	public AccountGetNoReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
