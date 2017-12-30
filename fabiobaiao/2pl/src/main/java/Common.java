import Communication.*;
import Log.*;
import io.atomix.catalyst.concurrent.ThreadContext;

public class Common {

    public static void registerSerializers(ThreadContext tc){

        registerLogs(tc);

        registerComm(tc);

        tc.serializer()
                .register(MethodCall.class);
    }

    private static void registerComm(ThreadContext tc) {
        tc.serializer()
                .register(AddResourceComm.class)
                .register(BeginComm.class)
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
