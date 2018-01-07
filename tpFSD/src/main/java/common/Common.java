package common;

import bank.*;
import bookstore.*;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;
import twophasecommit.Begin;
import twophasecommit.Commit;
import twophasecommit.Rollback;
import twophasecommit.TransactionContext;
import twophasecommit.communication.*;
import twophasecommit.logs.*;

public class Common {

    public static Address[] addresses = new Address[]{
            // Coordenador
            new Address("127.0.0.1:10000"),
            // Servers
            new Address("127.0.0.1:20000"),
            new Address("127.0.0.1:20001"),
            // Clients
            new Address("127.0.0.1:30000")
    };

    public static int STORE_SERVER_ID = 1;
    public static int STORE_ID = 1;
    public static int BANK_SERVER_ID = 2;
    public static int BANK_ID = 1;
    public static int STORE_ACCOUNT_ID = 0;

    public static void registerSerializers(ThreadContext tc){

        registerLogs(tc);

        registerComm(tc);

        registerClient(tc);

        registerReqs(tc);

        registerReps(tc);

        tc.serializer()
                .register(Book.class)
                .register(ObjRef.class);
    }

    private static void registerReps(ThreadContext tc) {
        tc.serializer()
                .register(AccountCreditRep.class)
                .register(AccountDebitRep.class)
                .register(AccountGetBalanceRep.class)
                .register(AccountGetNoRep.class)
                .register(AccountGetPaymentHistoryRep.class)
                .register(AccountPayRep.class)
                .register(BankGetAccountRep.class)
                .register(BankGetNameRep.class)
                .register(CartAddRep.class)
                .register(CartBuyRep.class)
                .register(CartClearRep.class)
                .register(CartGetContentRep.class)
                .register(CartRemoveRep.class)
                .register(StoreGetOrderHistoryRep.class)
                .register(StoreMakeCartRep.class)
                .register(StoreSearchRep.class);
    }

    private static void registerReqs(ThreadContext tc) {
        tc.serializer()
                .register(AccountCreditReq.class)
                .register(AccountDebitReq.class)
                .register(AccountGetBalanceReq.class)
                .register(AccountGetNoReq.class)
                .register(AccountGetPaymentHistoryReq.class)
                .register(AccountPayReq.class)
                .register(BankGetAccountReq.class)
                .register(BankGetNameReq.class)
                .register(CartAddReq.class)
                .register(CartBuyReq.class)
                .register(CartClearReq.class)
                .register(CartGetContentReq.class)
                .register(CartRemoveReq.class)
                .register(StoreGetOrderHistoryReq.class)
                .register(StoreMakeCartReq.class)
                .register(StoreSearchReq.class);


    }

    private static void registerClient(ThreadContext tc) {
        tc.serializer()
                //.register(Ack.class)
                .register(Begin.class)
                .register(Commit.class)
                .register(Rollback.class)
                .register(TransactionContext.class);
    }

    private static void registerComm(ThreadContext tc) {
        tc.serializer()
                .register(AddResourceComm.class)
                .register(CommitComm.class)
                .register(CommitedComm.class)
                .register(NotOkComm.class)
                .register(OkComm.class)
                .register(PrepareComm.class)
                .register(RollbackComm.class);

    }

    private static void registerLogs(ThreadContext tc) {
        tc.serializer()
                .register(AbortMarker.class)
                .register(BeginMarker.class)
                .register(CommitMarker.class)
                .register(CommittingMarker.class)
                .register(PreparedMarker.class)
                .register(PreparingMarker.class)
                .register(ResourceMarker.class);

    }
}
