package bookstore;

import common.Req;

public class CartClearReq extends Req {
	public CartClearReq() {}
	public CartClearReq(int cartId) { super(cartId); }
}
