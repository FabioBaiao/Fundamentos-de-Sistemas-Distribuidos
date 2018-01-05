package bookstore;

import common.Req;

public class CartGetContentReq extends Req {
	CartGetContentReq() {}
	CartGetContentReq(int cartId) { super(cartId); }
}
