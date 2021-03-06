package bank;

import java.util.List;
import java.util.Map;

public class LocalBank implements Bank {
	private Map<Integer, Account> accounts;

	@Override
	public void payFrom(int accountNo, double amount) throws NoSuchAccountException {
		Account a = accounts.get(accountNo);

		if (a == null)
			throw new NoSuchAccountException(accountNo);

		a.pay(amount);
	}

	@Override
	public List<Payment> getPaymentHistory(int accountNo) throws NoSuchAccountException {
		Account a = accounts.get(accountNo);

		if (a == null)
			throw new NoSuchAccountException(accountNo);

		return a.getPaymentHistory();
	}
}
