import Communication.*;
import Log.*;
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
        Set<Integer> indexesInUse = new TreeSet<>();


        Random r = new Random();

        Common.registerSerializers(tc);

        l.handler(PreparedLog.class, (index, p) -> {
            TransactionChanges transaction = new TransactionChanges(p.xid);
            activeTransactions.put(p.xid, transaction);

            indexesInUse.add(index);
            transaction.addIndex(index);
        });


        l.handler(CommitLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o prepared, mas não apagou o commit ??
            }
            else {
                tc.execute(() -> {
                    c.send(0, new CommitedComm(p.xid));
                });
                // persistir alterações ??
                // libertar locks
                // terminar transação
            }
        });

        l.handler(AbortLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o prepared, mas não apagou o commit ??
            }
            else {
                // recover initial status
                // release locks of transaction
                // terminar transação
            }
        });


        try {
            tc.execute(() -> l.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        c.handler(PrepareComm.class, (from, recv) -> {
            TransactionChanges transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                // Não conhecer uma transação em que participou significa que esta deve abortar
                // Não é necessário guardar nada no log
                tc.execute(() -> {
                    c.send(from, new NotOkComm(recv.xid));
                });
            }
            else {
                try {
                    int index = tc.execute(() ->
                        // adicionar objetos alterados
                        // adicionar objetos com lock
                        l.append(new PreparedLog(recv.xid, new ArrayList<>()))
                    ).join().get();

                    indexesInUse.add(index);
                    transaction.addIndex(index);

                    tc.execute(() -> {
                        c.send(from, new OkComm(recv.xid));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        c.handler(CommitComm.class, (from, recv) -> {
            if (activeTransactions.containsKey(recv.xid)) {
                try {
                    TransactionChanges transaction = activeTransactions.get(recv.xid);
                    int index = tc.execute(() ->
                        l.append(new CommitLog(recv.xid))
                    ).join().get();

                    /*indexesInUse.add(index);
                    transaction.addIndex(index);*/

                    tc.execute(() -> {
                        c.send(from, new CommitedComm(recv.xid));
                    });
                    // persistir alterações ??
                    // libertar locks
                    // terminar transação
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Commit já foi feito
                tc.execute(() -> {
                    c.send(from, new CommitedComm(recv.xid));
                });
            }
        });

        c.handler(RollbackComm.class, (from, recv) -> {
            if (activeTransactions.containsKey(recv.xid)) {
                try {
                    tc.execute(() ->
                            l.append(new AbortLog(recv.xid))
                    ).join().get();

                    // recover initial status
                    // release locks of transaction
                    // terminar transação
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        tc.execute(() -> {
            t.server().listen(addresses[id], connection -> {

                connection.handler(MethodCall.class, (recv) -> {

                    TransactionChanges transaction = activeTransactions.get(recv.xid);
                    if (transaction == null) {

                        try {
                            Object o = c.sendAndReceive(0, new AddResourceComm(recv.xid)).get();

                            if (o instanceof RollbackComm) {
                                return Futures.completedFuture(new RollbackComm(recv.xid));
                            }

                            transaction = new TransactionChanges(recv.xid);
                            activeTransactions.put(recv.xid, transaction);

                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    // method
                    // append initial status of each modified object ??
                    // lock used objects

                    return Futures.completedFuture(null);
                });
            });
        });

        tc.execute(() -> c.open());
    }
}
