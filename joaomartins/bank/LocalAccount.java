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
	public void pay(double ammount) {
		pay(ammount, null);
	}

	@Override
	public void pay(double ammount, String description) {
/*
		if (balance >= ammount) {
			balance -= ammount;
			paymentHistory.add(new Payment(ammount, description));
		} else {
			throw new NotEnoughFundsException(ammount);
		}
*/
		paymentHistory.add(new Payment(ammount, description)); 
	}

	@Override
	public List<Payment> getPaymentHistory() { return paymentHistory; }
}
