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
import java.util.concurrent.atomic.AtomicInteger;

public class Coordinator {

    private ThreadContext tc;
    private Transport t;
    private Log l;
    private Clique c;

    private Map<Integer, TransactionInfo> activeTransactions;
    private AtomicInteger transactionIdCounter;
    private TreeSet<Integer> indexesInUse;

    private Address address;

    private Coordinator(Address[] addresses, int id) {
        this.tc = new SingleThreadContext("srv-%d", new Serializer());
        this.l = new Log("" + id);
        this.t = new NettyTransport();
        this.c = new Clique(t, id, addresses);

        this.activeTransactions = new HashMap<>();
        this.transactionIdCounter = new AtomicInteger(0);
        this.indexesInUse = new TreeSet<>();

        this.address = addresses[id];
    }

    private void init() {
        Common.registerSerializers(tc);

        rpcLogHandlers();
        twoPhaseCommitLogHandlers();

        try {
            tc.execute(() -> l.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        initClique();

        // transações não terminadas do log
        for (TransactionInfo transaction : activeTransactions.values()) {
            if (transaction.isPreparing())
                sendPrepares(transaction);
            else
                rollback(transaction);
        }

        rpcClientHandlers();
    }

    private void initClique() {
        rpcCliqueHandlers();
        twoPhaseCommitCliqueHandlers();

        try {
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private int begin() {
        // para não sobrepor
        while (activeTransactions.containsKey(transactionIdCounter.incrementAndGet()));

        int xid = transactionIdCounter.get();

        TransactionInfo transaction = new TransactionInfo(xid);

        try {
            int index = tc.execute(() -> l.append(new BeginLog(xid))).join().get();

            transaction.addIndex(index);
            indexesInUse.add(index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        activeTransactions.put(xid, transaction);

        return xid;
    }

    private void addParticipant(TransactionInfo transaction, int participant) {
        try {
            int index = tc.execute(() ->
                    l.append(new ResourceLog(transaction.id, participant))
            ).join().get();

            transaction.addIndex(index);
            indexesInUse.add(index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        transaction.addParticipant(participant);
    }

    private void sendPrepares(TransactionInfo transaction) {
        for (int participant : transaction.participants) {
            tc.execute(() -> {
                c.send(participant, new PrepareComm(transaction.id));
            });
        }
    }

    private void prepare(TransactionInfo transaction) {
        try {
            int index = tc.execute(() ->
                    l.append(new Preparing(transaction.id, transaction.participants))
            ).join().get();

            transaction.addIndex(index);
            indexesInUse.add(index);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        sendPrepares(transaction);
    }

    private void commit(TransactionInfo transaction) {

        tc.execute(() -> {
            l.append(new Commit(transaction.id));
        });
        for (int i : transaction.prepared) {
            tc.execute(() -> {
                c.send(i, new CommitComm(transaction.id));
            });
        }

        removeTransaction(transaction);
    }


    private void rollback(TransactionInfo transaction) {
        // adicionar marcador no inicio ou no fim ??
        // tem de ser adicionado, pois não é garantido que o preparing seja removido
        tc.execute(() -> {
            l.append(new Abort(transaction.id));
        });
        for (int i : transaction.participants) {
            tc.execute(() -> {
                c.send(i, new RollbackComm(transaction.id));
            });
        }

        removeTransaction(transaction);
    }

    private void removeTransaction(TransactionInfo transaction) {

        activeTransactions.remove(transaction.id);
        for (int index : transaction.indexes) {
            indexesInUse.remove(index);
        }
        if (indexesInUse.size() > 0) {
            tc.execute(() -> {
                l.trim(indexesInUse.first());
            });
        }
        else {
            tc.execute(() -> {
                l.trim(Integer.MAX_VALUE);
            });
        }
    }

    private void twoPhaseCommitLogHandlers() {

        l.handler(Preparing.class, (index, p) -> {
            System.out.println("Preparing found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            transaction.setPreparing();

            transaction.addIndex(index);
            indexesInUse.add(index);
        });

        l.handler(Commit.class, (index, p) -> {
            System.out.println("Commit found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            removeTransaction(transaction);
        });

        l.handler(Abort.class, (index, p) -> {
            System.out.println("Abort found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            removeTransaction(transaction);
        });
    }

    private void twoPhaseCommitCliqueHandlers() {
        c.handler(OkComm.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                tc.execute(() -> {
                    c.send(from, new RollbackComm(recv.xid));
                });
            }
            else {
                transaction.addPrepared(from);
                if (transaction.allPrepared())
                    commit(transaction);
            }
        });

        c.handler(NotOkComm.class, (from, recv) ->  {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                tc.execute(() -> {
                    c.send(from, new RollbackComm(recv.xid));
                });
            }
            else {
                rollback(transaction);
            }
        });
    }

    private void rpcLogHandlers() {

        l.handler(BeginLog.class, (index, p) -> {
            System.out.println("BeginLog found");
            TransactionInfo transaction = new TransactionInfo(p.xid);
            activeTransactions.put(p.xid, transaction);

            transaction.addIndex(index);
            indexesInUse.add(index);
        });

        l.handler(ResourceLog.class, (index, p) -> {
            System.out.println("Log.ResourceLog found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            transaction.addParticipant(p.participant);

            transaction.addIndex(index);
            indexesInUse.add(index);
        });
    }

    private void rpcCliqueHandlers() {
        c.handler(AddResourceComm.class, (from, recv) -> {

            TransactionInfo transaction = activeTransactions.get(recv.xid);
            addParticipant(transaction, from);
            // send what??
        });
    }

    private void rpcClientHandlers() {
        t.server().listen(address, connection -> {

            connection.handler(BeginComm.class, (recv) -> {
                return Futures.completedFuture(begin());
            });

            connection.handler(Commit.class, (recv) -> {
                TransactionInfo transaction = activeTransactions.get(recv.xid);
                prepare(transaction);
            });
        });
    }

    public static void main(String[] args) {
        Address[] addresses = new Address[]{
                new Address("127.0.0.1:10000"),
                new Address("127.0.0.1:10001"),
                new Address("127.0.0.1:10002")
        };

        int id = 0;

        Coordinator c = new Coordinator(addresses, id);

        c.init();
    }
}
