import Communication.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Server {

    public static void main(String[] args){
        Address[] addresses = new Address[]{
                new Address("127.0.0.1:10000"),
                new Address("127.0.0.1:10001"),
                new Address("127.0.0.1:10002")
        };

        int id = Integer.parseInt(args[0]);

        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
        Transport t = new NettyTransport();
        Clique c = new Clique(t, id, addresses);
        Log l = new Log("" + id);

        Map<Integer, TransactionChanges> activeTransactions = new HashMap<>();



        Random r = new Random();

        Common.registerSerializers(tc);

        tc.execute(() -> {
            l.handler(Abort.class, (i, m) -> {
            });
            l.handler(Commit.class, (i, m) -> {
            });
            l.handler(Prepared.class, (i, m) -> {
            });
        });

        try {
            tc.execute(() -> l.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        c.handler(PrepareComm.class, (from, recv) -> {
            switch (r.nextInt(3)) {
                case 0:
                case 1:
                    // ordem ??
                    tc.execute(() -> {
                        l.append(new Prepared(recv.xid));
                    });
                    tc.execute(() -> {
                        c.send(from, new OkComm(recv.xid));
                    });
                    break;
                case 2:
                    tc.execute(() -> {
                        l.append(new Abort(recv.xid));
                    });
                    tc.execute(() -> {
                        c.send(from, new NotOkComm(recv.xid));
                    });
                    // recover initial status
                    // release locks of transaction
                    break;
            }
        });

        c.handler(CommitComm.class, (from, recv) -> {
            tc.execute(() -> {
                l.append(new Commit(recv.xid));
            });
            System.out.println("Commit");
        });

        c.handler(RollbackComm.class, (from, recv) -> {
            tc.execute(() -> {
                l.append(new Abort(recv.xid));
            });
            // recover initial status
            // release locks of transaction
            System.out.println("Abort");
        });

        tc.execute(() -> {
            t.server().listen(addresses[id], connection -> {

                connection.handler(MethodCall.class, (recv) -> {

                    TransactionChanges transaction = activeTransactions.get(recv.xid);
                    if (transaction == null) {
                        transaction = new TransactionChanges(recv.xid);
                        activeTransactions.put(recv.xid, transaction);

                        c.send(0, new AddResourceComm(recv.xid));
                    }
                    // method
                    // append initial status of each modified object
                    // lock used objects

                    return Futures.completedFuture(null);
                });
            });
        });

        tc.execute(() -> c.open());
    }
}
