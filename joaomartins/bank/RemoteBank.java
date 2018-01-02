package bank;

import io.atomix.catalyst.transport.Address;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RemoteBank implements Bank {
	private final DistributedObjectsRuntime dor;
	private final int id;
	private final Address a;

	public RemoteBank(DistributedObjectsRuntime dor, int id, Address a) {
		this.dor = dor;
		this.id = id;
		this.a = a;
	}

	@Override
	public void payFrom(int accountNo, double amount) throws NoSuchAccountException {
		try {
			BankPayFromRep r = (BankPayFromRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankPayFromReq(id, accountNo, amount))
			).join().get();

			if (!r.isOk()) {
				throw new NoSuchAccountException(r.getError());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Payment> getPaymentHistory(int accountNo) throws NoSuchAccountException {
		try {
			BankGetPaymentHistoryRep r = (BankGetPaymentHistoryRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankGetPaymentHistoryReq(id, accountNo))
			).join().get();

			String error = r.getError();
			if (error == null) {
				return r.getPaymentHistory();
			} else {
				throw new NoSuchAccountException(error);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
/*
		@Override
		public int openAccount() {

		}

		@Override
		public void closeAccount(int accountNo) {

		}
*/

}
