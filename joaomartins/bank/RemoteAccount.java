package bank;

import io.atomix.catalyst.transport.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RemoteAccount implements Account {
	private final DistributedObjectsRuntime dor;
	private final int id;
	private final Address a;

	public RemoteAccount(DistributedObjectsRuntime dor, int id, Address a) {
		this.dor = dor;
		this.id = id;
		this.a = a;
	}

	@Override
	public void pay(double amount) { pay(amount, null); }

	@Override
	public void pay(double amount, String description) {
		try {
			AccountPayRep r = (AccountPayRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountPayReq(id, amount, description))
			).join().get();
// NOTE: Currently LocalAccount.pay always succeeds
/*		
			if (!r.isOk()) {
				throw ...
			}
*/
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Payment> getPaymentHistory() {
		try {
			AccountGetPaymentHistoryRep r = (AccountGetPaymentHistoryRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new AccountGetPaymentHistoryReq(id))
			).join().get();
			
			return r.getPaymentHistory();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
