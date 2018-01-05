package bookstore;

import common.Req;
import twophasecommit.TransactionContext;

public class CartClearReq extends Req {
	public CartClearReq() {}
	public CartClearReq(TransactionContext txCtxt, int cartId) { super(txCtxt, cartId); }
}
