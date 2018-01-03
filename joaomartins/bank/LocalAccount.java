package bank;

import java.util.ArrayList;
import java.util.List;

public class LocalAccount implements Account {
//	private double balance;
	private final List<Payment> paymentHistory;

	public LocalAccount() {
// 		balance = 0.0;
		paymentHistory = new ArrayList<>();
	}

	@Override
	public void pay(double amount) {
		pay(amount, null);
	}

	@Override
	public void pay(double amount, String description) {
/*
		if (balance >= amount) {
			balance -= amount;
			paymentHistory.add(new Payment(amount, description));
		} else {
			throw new NotEnoughFundsException(amount);
		}
*/
		paymentHistory.add(new Payment(amount, description)); 
	}

	@Override
	public List<Payment> getPaymentHistory() { return paymentHistory; }
}
