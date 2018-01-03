package bank;

import java.util.List;

// Simplification: Accounts always have enough money for paying.
public interface Account {
	void pay(double amount);
	void pay(double amount, String description);
	List<Payment> getPaymentHistory();
}
