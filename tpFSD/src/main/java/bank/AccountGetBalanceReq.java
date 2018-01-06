package bank;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class AccountGetBalanceReq extends AbstractReq {
	public AccountGetBalanceReq() {}
	public AccountGetBalanceReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
