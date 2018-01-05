package bank;

import common.Req;
import twophasecommit.TransactionContext;

public class BankGetNameReq extends Req {
	public BankGetNameReq() {}
	public BankGetNameReq(TransactionContext txCtxt, int bankId) { super(txCtxt, bankId); }
}
