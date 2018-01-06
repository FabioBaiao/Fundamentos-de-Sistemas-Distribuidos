package bookstore;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class CartGetContentReq extends AbstractReq {
	CartGetContentReq() {}
	CartGetContentReq(TransactionContext txCtxt, int cartId) { super(txCtxt, cartId); }
}
