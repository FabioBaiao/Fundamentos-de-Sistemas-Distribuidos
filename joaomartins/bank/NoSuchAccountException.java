package bank;

public class NoSuchAccountException extends Exception {
	public NoSuchAccountException(int accountId) { super("Account " + accountId + " doesn't exist"); }
	public NoSuchAccountException(String message) { super(message); }
}
