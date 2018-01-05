package bank;

import common.DistributedObjectsRuntime;
import common.ObjRef;
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
	public String getName() {
		try {
			BankGetNameRep r = (BankGetNameRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankGetNameReq(id))
			).join().get();
			
			return r.getName();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Account getAccount(int accountNo) {
		try {
			BankGetAccountRep r = (BankGetAccountRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankGetAccountReq(id, accountNo))
			).join().get();

			ObjRef accountRef = r.getAccountRef();
			return (accountRef == null) ? null : (Account) dor.objImport(accountRef);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class RemoteAccount implements Account {
		private final DistributedObjectsRuntime dor;
		private final int id;
		private final Address a;

		public RemoteAccount(DistributedObjectsRuntime dor, int id, Address a) {
			this.dor = dor;
			this.id = id;
			this.a = a;
		}

		public ObjRef getObjRef() { return new ObjRef(a, id, "Bank.Account"); }

		@Override
		public int getNo() {
			try {
				AccountGetNoRep r = (AccountGetNoRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountGetNoReq(id))
				).join().get();
				
				return r.getNo();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return -1;
		}

		@Override
		public double getBalance() {
			try {
				AccountGetBalanceRep r = (AccountGetBalanceRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountGetBalanceReq(id))
				).join().get();
				
				return r.getBalance();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return 0.0;
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

		@Override
		public void credit(double amount) {
			try {
				AccountCreditRep r = (AccountCreditRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountCreditReq(id, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void debit(double amount) {
			try {
				AccountDebitRep r = (AccountDebitRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountDebitReq(id, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void pay(double amount, String description, Account dst) {
			try {
				RemoteAccount remoteDst = (RemoteAccount) dst;
				ObjRef dstRef = new ObjRef(remoteDst.a, remoteDst.id, "Bank.Account");

				AccountPayRep r = (AccountPayRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountPayReq(id, amount, description, dstRef))
				).join().get();
			} catch (InterruptedException | ExecutionException | ClassCastException e) {
				e.printStackTrace();
			}
		}
	}
}
