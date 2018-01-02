package bank;

import java.util.List;

// Simplification: Accounts always have enough money for paying.
public interface Account {
	void pay(double ammount);
	void pay(double ammount, String description);
	List<Payment> getPaymentHistory();
}
