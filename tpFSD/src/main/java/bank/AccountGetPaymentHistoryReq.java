package bank;

import common.Req;
import twophasecommit.TransactionContext;

public class AccountGetPaymentHistoryReq extends Req {
	public AccountGetPaymentHistoryReq() {}
	public AccountGetPaymentHistoryReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
