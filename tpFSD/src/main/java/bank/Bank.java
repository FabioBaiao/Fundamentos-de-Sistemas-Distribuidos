package bank;

import twophasecommit.TransactionContext;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Bank {
	String getName(TransactionContext txCtxt);
	Account getAccount(TransactionContext txCtxt, int accountNo) throws Exception;

	interface Account {
		int getNo(TransactionContext txCtxt);
		double getBalance(TransactionContext txCtxt);
		List<Payment> getPaymentHistory(TransactionContext txCtxt);
		void credit(TransactionContext txCtxt, double amount);
		void debit(TransactionContext txCtxt, double amount);
		void pay(TransactionContext txCtxt, double amount, String description, Account dst);
	}
}
