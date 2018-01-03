package bank;

import java.util.List;

public interface Bank {
	String getName();
	Account getAccount(int accountNo);

	interface Account {
		int getNo();
		double getBalance();
		List<Payment> getPaymentHistory();
		void credit(double amount);
		void debit(double amount);
		void pay(double amount, String description, Bank.Account dst);
	}
}
