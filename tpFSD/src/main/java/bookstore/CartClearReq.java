package bookstore;

import common.AbstractReq;
import twophasecommit.TransactionContext;

public class CartClearReq extends AbstractReq {
	public CartClearReq() {}
	public CartClearReq(TransactionContext txCtxt, int cartId) { super(txCtxt, cartId); }
}
