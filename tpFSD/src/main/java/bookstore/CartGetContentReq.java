package bookstore;

import common.Req;
import twophasecommit.TransactionContext;

public class CartGetContentReq extends Req {
	CartGetContentReq() {}
	CartGetContentReq(TransactionContext txCtxt, int cartId) { super(txCtxt, cartId); }
}
