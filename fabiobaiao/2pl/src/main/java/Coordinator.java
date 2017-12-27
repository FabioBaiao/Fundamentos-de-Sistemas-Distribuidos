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
        registerSerializers();

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

    private TransactionInfo begin(int client) {
        // para não sobrepor
        while (activeTransactions.containsKey(transactionIdCounter.incrementAndGet()));

        int xid = transactionIdCounter.get();

        TransactionInfo transaction = new TransactionInfo(xid, client);

        try {
            int index = tc.execute(() -> l.append(new Begin(xid, client))).join().get();

            transaction.addIndex(index);
            indexesInUse.add(index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        activeTransactions.put(xid, transaction);

        return transaction;
    }

    private void addParticipant(TransactionInfo transaction, int participant) {
        try {
            int index = tc.execute(() ->
                    l.append(new Participant(transaction.id, participant))
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
                c.send(participant, new Prepare(transaction.id));
            });
        }
    }

    private void prepare(TransactionInfo transaction) {
        int index = 0;
        try {
            index = tc.execute(() ->
                    l.append(new Preparing(transaction.id, transaction.participants))
            ).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        transaction.addIndex(index);
        indexesInUse.add(index);

        sendPrepares(transaction);
    }

    private void commit(TransactionInfo transaction) {

        // adicionar marcador no inicio ou no fim ??
        // tem de ser adicionado, pois não é garantido que o preparing seja removido
        tc.execute(() -> {
            l.append(new Commit(transaction.id));
        });
        for (int i : transaction.prepared) {
            tc.execute(() -> {
                c.send(i, new Commit(transaction.id));
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
                c.send(i, new Rollback(transaction.id));
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
        c.handler(Ok.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                tc.execute(() -> {
                    c.send(from, new Rollback(recv.xid));
                });
            }
            else {
                transaction.addPrepared(from);
                if (transaction.allPrepared())
                    commit(transaction);
            }
        });

        c.handler(NotOk.class, (from, recv) ->  {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                tc.execute(() -> {
                    c.send(from, new Rollback(recv.xid));
                });
            }
            else {
                rollback(transaction);
            }
        });
    }

    private void rpcLogHandlers() {

        l.handler(Begin.class, (index, p) -> {
            System.out.println("Begin found");
            TransactionInfo transaction = new TransactionInfo(p.xid, p.client);
            activeTransactions.put(p.xid, transaction);

            transaction.addIndex(index);
            indexesInUse.add(index);
        });

        l.handler(Participant.class, (index, p) -> {
            System.out.println("Participant found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            transaction.addParticipant(p.participant);

            transaction.addIndex(index);
            indexesInUse.add(index);
        });
    }

    private void rpcCliqueHandlers() {
        c.handler(AddResource.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            addParticipant(transaction, from);
            // send what??
        });
    }

    private void rpcClientHandlers() {
        t.server().listen(address, connection -> {

            connection.handler(Begin.class, (recv) -> {
                TransactionInfo transaction = begin(recv.client);
                // send context
            });

            connection.handler(Commit.class, (recv) -> {
                TransactionInfo transaction = activeTransactions.get(recv.xid);
                prepare(transaction);
                // send confirmation ??
            });
        });
    }

    private void registerSerializers() {
        tc.serializer()
                .register(Abort.class)
                .register(Begin.class)
                .register(Commit.class)
                .register(NotOk.class)
                .register(Ok.class)
                .register(Participant.class)
                .register(Prepare.class)
                .register(Prepared.class)
                .register(Preparing.class)
                .register(Rollback.class);
    }

    private void testTwoPhaseCommit() {
        // initialization
        TransactionInfo transaction = begin(-1);
        for (int i = 1; i < 3; i++) {
            addParticipant(transaction, i);
        }

        //client commites
        prepare(transaction);
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


        c.testTwoPhaseCommit();

    }
}
