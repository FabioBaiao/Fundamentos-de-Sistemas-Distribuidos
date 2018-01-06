package bank;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class AccountGetPaymentHistoryReq extends AbstractReq {
	public AccountGetPaymentHistoryReq() {}
	public AccountGetPaymentHistoryReq(TransactionContext txCtxt, int accountId) { super(txCtxt, accountId); }
}
