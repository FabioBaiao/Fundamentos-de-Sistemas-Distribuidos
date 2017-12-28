import Communication.*;
import Log.BeginLog;
import Log.ResourceLog;
import io.atomix.catalyst.concurrent.ThreadContext;

public class Common {

    public static void registerSerializers(ThreadContext tc){
        tc.serializer()
                .register(AddResourceComm.class)
                .register(BeginComm.class)
                .register(CommitComm.class)
                .register(OkComm.class)
                .register(PrepareComm.class);

        tc.serializer()
                .register(BeginLog.class);


        tc.serializer()
                .register(Abort.class)
                .register(Commit.class)
                .register(NotOkComm.class)
                .register(ResourceLog.class)
                .register(Prepared.class)
                .register(Preparing.class)
                .register(RollbackComm.class);

        tc.serializer()
                .register(MethodCall.class);
    }
}
