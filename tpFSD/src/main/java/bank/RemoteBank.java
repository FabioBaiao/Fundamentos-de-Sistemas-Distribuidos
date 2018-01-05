package bank;

import common.DistributedObjectsRuntime;
import common.ObjRef;
import io.atomix.catalyst.transport.Address;
import twophasecommit.TransactionContext;

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
	public String getName(TransactionContext txCtxt) {
		try {
			BankGetNameRep r = (BankGetNameRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankGetNameReq(txCtxt, id))
			).join().get();
			
			return r.getName();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Account getAccount(TransactionContext txCtxt, int accountNo) {
		try {
			BankGetAccountRep r = (BankGetAccountRep) dor.tc.execute(() ->
				dor.cons.get(a).sendAndReceive(new BankGetAccountReq(txCtxt, id, accountNo))
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
		public int getNo(TransactionContext txCtxt) {
			try {
				AccountGetNoRep r = (AccountGetNoRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountGetNoReq(txCtxt, id))
				).join().get();
				
				return r.getNo();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return -1;
		}

		@Override
		public double getBalance(TransactionContext txCtxt) {
			try {
				AccountGetBalanceRep r = (AccountGetBalanceRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountGetBalanceReq(txCtxt, id))
				).join().get();
				
				return r.getBalance();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return 0.0;
		}

		@Override
		public List<Payment> getPaymentHistory(TransactionContext txCtxt) {
			try {
				AccountGetPaymentHistoryRep r = (AccountGetPaymentHistoryRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountGetPaymentHistoryReq(txCtxt, id))
				).join().get();
			
				return r.getPaymentHistory();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void credit(TransactionContext txCtxt, double amount) {
			try {
				AccountCreditRep r = (AccountCreditRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountCreditReq(txCtxt, id, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void debit(TransactionContext txCtxt, double amount) {
			try {
				AccountDebitRep r = (AccountDebitRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountDebitReq(txCtxt, id, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void pay(TransactionContext txCtxt, double amount, String description, Account dst) {
			try {
				RemoteAccount remoteDst = (RemoteAccount) dst;
				ObjRef dstRef = new ObjRef(remoteDst.a, remoteDst.id, "Bank.Account");

				AccountPayRep r = (AccountPayRep) dor.tc.execute(() ->
					dor.cons.get(a).sendAndReceive(new AccountPayReq(txCtxt, id, amount, description, dstRef))
				).join().get();
			} catch (InterruptedException | ExecutionException | ClassCastException e) {
				e.printStackTrace();
			}
		}
	}
}
