package bank;

import twophasecommit.TransactionContext;

import java.util.*;

public class LocalBank implements Bank {
	private final String name;
	private final Map<Integer, Account> accounts;

	public LocalBank(String name, Map<Integer, Account> accounts) {
		this.name = name;
		this.accounts = new HashMap<>(accounts);
	}

	@Override
	public String getName(TransactionContext txCtxt) { return name; }

	@Override
	public Account getAccount(TransactionContext txCtxt, int accountNo) {
		return accounts.get(accountNo);
	}

	public static class LocalAccount implements Account {
		private final int no;
		private double balance;
		private SortedSet<Payment> paymentHistory;

		public LocalAccount(int no) {
			this(no, 0.0);
		}

		public LocalAccount(int no, double initialBalance) {
			this.no = no;
			this.balance = initialBalance;
			this.paymentHistory = new TreeSet<>();
		}

		@Override
		public int getNo(TransactionContext txCtxt) { return no; }

		@Override
		public double getBalance(TransactionContext txCtxt) { return balance; }

		@Override
		public SortedSet<Payment> getPaymentHistory(TransactionContext txCtxt) { return paymentHistory; }

		@Override
		public void credit(TransactionContext txCtxt, double amount) { balance += amount; }

		@Override
		public void debit(TransactionContext txCtxt, double amount) { balance -= amount; } // Simplification: Allow negative balances

		@Override
		public void pay(TransactionContext txCtxt, double amount, String description, Account dst) {
			this.debit(txCtxt, amount);
			dst.credit(txCtxt, amount);
			paymentHistory.add(new Payment(amount, description));
		}
	}
}
