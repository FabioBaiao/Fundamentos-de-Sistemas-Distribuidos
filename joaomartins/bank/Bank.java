package bank;

import java.util.List;

public interface Bank {
/*
-> Methods that a complete interface would have:

	void credit(double ammount, int accountNo);
	void debit(double ammount, int accountNo);
	double getBalance(int accountNo);
	void transfer(double ammount, int srcAccountId, int dstAccountId); // internal transfer
	void transfer(double ammount, int srcAccountId, Bank dstBank, int dstAccountId); // external transfer
	int openAccount(); // Creates a new account and returns its id.
	void closeAccount(int accountNo);
*/

	// Simplification: Accounts always have enough money for paying.
	void payFrom(int accountNo, double ammount) throws NoSuchAccountException;
	List<Payment> getPaymentHistory(int accountNo) throws NoSuchAccountException;
}
