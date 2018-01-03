package bank;

import java.util.List;

public interface Bank {
/*
-> Methods that a complete interface would have:

	void credit(double amount, int accountNo);
	void debit(double amount, int accountNo);
	double getBalance(int accountNo);
	void transfer(double amount, int srcAccountId, int dstAccountId); // internal transfer
	void transfer(double amount, int srcAccountId, Bank dstBank, int dstAccountId); // external transfer
	int openAccount(); // Creates a new account and returns its id.
	void closeAccount(int accountNo);
*/

	// Simplification: Accounts always have enough money for paying.
	void payFrom(int accountNo, double amount) throws NoSuchAccountException;
	List<Payment> getPaymentHistory(int accountNo) throws NoSuchAccountException;
}
