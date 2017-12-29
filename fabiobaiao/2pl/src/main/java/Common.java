import Communication.*;
import Log.*;
import io.atomix.catalyst.concurrent.ThreadContext;

public class Common {

    public static void registerSerializers(ThreadContext tc){
        registerComm(tc);

        registerLogs(tc);

        tc.serializer()
                .register(Abort.class)
                .register(NotOkComm.class)
                .register(PreparedLog.class)
                .register(RollbackComm.class);

        tc.serializer()
                .register(MethodCall.class);
    }

    private static void registerComm(ThreadContext tc) {
        tc.serializer()
                .register(AddResourceComm.class)
                .register(BeginComm.class)
                .register(CommitComm.class)
                .register(OkComm.class)
                .register(PrepareComm.class);

    }

    private static void registerLogs(ThreadContext tc) {
        tc.serializer()
                .register(BeginLog.class)
                .register(CommitLog.class)
                .register(PreparingLog.class)
                .register(ResourceLog.class);

    }
}
