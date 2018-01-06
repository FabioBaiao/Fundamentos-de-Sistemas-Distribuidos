package bank;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class BankGetNameReq extends AbstractReq {
	public BankGetNameReq() {}
	public BankGetNameReq(TransactionContext txCtxt, int bankId) { super(txCtxt, bankId); }
}
