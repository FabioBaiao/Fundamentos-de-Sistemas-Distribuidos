package bank;

import common.DistributedObjectsRuntime;
import common.ObjRef;
import io.atomix.catalyst.transport.Address;
import twophasecommit.Rollback;
import twophasecommit.TransactionContext;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RemoteBank implements Bank {
	private final DistributedObjectsRuntime dor;
	private final int objectId;
	private final int cliqueId;

	public RemoteBank(DistributedObjectsRuntime dor, int cliqueId, int objectId) {
		this.dor = dor;
		this.cliqueId = cliqueId;
		this.objectId = objectId;
	}

	@Override
	public String getName(TransactionContext txCtxt) {
		try {
			BankGetNameRep r = (BankGetNameRep) dor.tc.execute(() ->
				dor.c.sendAndReceive(cliqueId, new BankGetNameReq(txCtxt, objectId))
			).join().get();

			/*BankGetNameRep r = (BankGetNameRep) dor.tc.execute(() ->
				dor.cons.get(cliqueId).sendAndReceive(new BankGetNameReq(txCtxt, objectId))
			).join().get();*/
			
			return r.getName();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Account getAccount(TransactionContext txCtxt, int accountNo) throws Exception{

		Object o = dor.tc.execute(() ->
			dor.c.sendAndReceive(cliqueId, new BankGetAccountReq(txCtxt, objectId, accountNo))
		).join().get();

		if (o instanceof Rollback)
			throw new Exception("Rollback");

		BankGetAccountRep r = (BankGetAccountRep) o;
		ObjRef accountRef = r.getAccountRef();
		return (accountRef == null) ? null : (Account) dor.objImport(accountRef);

	}

	public static class RemoteAccount implements Account {
		private final DistributedObjectsRuntime dor;
		private final int objectId;
		private final int cliqueId;

		public RemoteAccount(DistributedObjectsRuntime dor, int objectId, int cliqueId) {
			this.dor = dor;
			this.objectId = objectId;
			this.cliqueId = cliqueId;
		}

		public ObjRef getObjRef() { return new ObjRef(cliqueId, objectId, "Bank.Account"); }

		@Override
		public int getNo(TransactionContext txCtxt) {
			try {
				AccountGetNoRep r = (AccountGetNoRep) dor.tc.execute(() ->
					dor.c.sendAndReceive(cliqueId, new AccountGetNoReq(txCtxt, objectId))
				).join().get();


				/*AccountGetNoRep r = (AccountGetNoRep) dor.tc.execute(() ->
					dor.cons.get(cliqueId).sendAndReceive(new AccountGetNoReq(txCtxt, objectId))
				).join().get();*/
				
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
					dor.c.sendAndReceive(cliqueId, new AccountGetBalanceReq(txCtxt, objectId))
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
					dor.c.sendAndReceive(cliqueId, new AccountGetPaymentHistoryReq(txCtxt, objectId))
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
					dor.c.sendAndReceive(cliqueId, new AccountCreditReq(txCtxt, objectId, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void debit(TransactionContext txCtxt, double amount) {
			try {
				AccountDebitRep r = (AccountDebitRep) dor.tc.execute(() ->
					dor.c.sendAndReceive(cliqueId, new AccountDebitReq(txCtxt, objectId, amount))
				).join().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void pay(TransactionContext txCtxt, double amount, String description, Account dst) {
			try {
				RemoteAccount remoteDst = (RemoteAccount) dst;
				ObjRef dstRef = new ObjRef(remoteDst.cliqueId, remoteDst.objectId, "Bank.Account");

				AccountPayRep r = (AccountPayRep) dor.tc.execute(() ->
					dor.c.sendAndReceive(cliqueId, new AccountPayReq(txCtxt, objectId, amount, description, dstRef))
				).join().get();
			} catch (InterruptedException | ExecutionException | ClassCastException e) {
				e.printStackTrace();
			}
		}
	}
}
