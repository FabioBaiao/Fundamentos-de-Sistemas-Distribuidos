package bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LocalBank implements Bank {
	private final String name;
	private final Map<Integer, Account> accounts;

	public LocalBank(String name) {
		this.name = name;
		this.accounts = new HashMap<>();
	}

	@Override
	public String getName() { return name; }

	@Override
	public Account getAccount(int accountNo) {
		return accounts.get(accountNo);
	}

	public static class LocalAccount implements Bank.Account {
		private final int no;
		private double balance;
		private List<Payment> paymentHistory;

		public LocalAccount(int no) {
			this(no, 0.0);
		}

		public LocalAccount(int no, double initialBalance) {
			this.no = no;
			this.balance = initialBalance;
			this.paymentHistory = new ArrayList<>();
		}

		@Override
		public int getNo() { return no; }

		@Override
		public double getBalance() { return balance; }

		@Override
		public List<Payment> getPaymentHistory() { return paymentHistory; }

		@Override
		public void credit(double amount) { balance += amount; }

		@Override
		public void debit(double amount) { balance -= amount; } // Simplification: Allow negative balances

		@Override
		public void pay(double amount, String description, Account dst) {
			this.debit(amount);
			dst.credit(amount);
			paymentHistory.add(new Payment(amount, description));
		}
	}
}
