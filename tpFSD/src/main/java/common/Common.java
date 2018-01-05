package common;

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

    public static void registerSerializers(ThreadContext tc){

        registerLogs(tc);

        registerComm(tc);

        registerClient(tc);

        /*tc.serializer()
                .register(MethodCall.class)
                .register(Reply.class);*/
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
                .register(AbortLog.class)
                .register(BeginLog.class)
                .register(CommitLog.class)
                .register(CommittingLog.class)
                .register(PreparedLog.class)
                .register(PreparingLog.class)
                .register(ResourceLog.class);

    }
}
