import Communication.*;
import Log.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Coordinator {

    private ThreadContext tc;
    private Log l;
    private Clique c;

    private Map<Integer, TransactionInfo> activeTransactions;
    private AtomicInteger transactionIdCounter;
    private TreeSet<Integer> indexesInUse;

    public Coordinator(int id) {
        this.tc = new SingleThreadContext("srv-%d", new Serializer());
        this.l = new Log("" + id);
        Transport t = new NettyTransport();
        this.c = new Clique(t, id, Common.addresses);

        this.activeTransactions = new HashMap<>();
        this.transactionIdCounter = new AtomicInteger(0);
        this.indexesInUse = new TreeSet<>();
    }

    public void init() {
        Common.registerSerializers(tc);

        // criar handlers do log para serem executados durante o open()
        rpcLogHandlers();
        twoPhaseCommitLogHandlers();

        try {
            tc.execute(() -> l.open()).join().get();
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // iniciar handlers dos servidores para não perder respostas relativas ao 2PC
        twoPhaseCommitServerHandlers();

        // transações não terminadas do log
        for (TransactionInfo transaction : activeTransactions.values()) {
            switch (transaction.status) {
                case RUNNING:
                    rollback(transaction);
                    break;
                case PREPARING:
                    sendPrepares(transaction);
                    break;
                case COMMITTING:
                    sendCommits(transaction);
                    break;
            }
        }

        // inicializar handlers de rpc para permitir novas transações
        rpcServerHandlers();
        rpcClientHandlers();
    }

    private int begin(int client) {
        // para não sobrepor
        while (activeTransactions.containsKey(transactionIdCounter.incrementAndGet()));

        int xid = transactionIdCounter.get();

        TransactionInfo transaction = new TransactionInfo(xid, client);

        try {
            int index = tc.execute(() -> l.append(new BeginLog(xid, client))).join().get();

            addIndex(transaction, index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        activeTransactions.put(xid, transaction);

        return xid;
    }

    private Object addResource(TransactionInfo transaction, int participant) {
        if (transaction.containsParticipant(participant)){
            rollback(transaction);
            return Futures.completedFuture(new RollbackComm(transaction.id));
        }
        else {
            addParticipant(transaction, participant);
            return Futures.completedFuture(new Ack());
        }
    }

    private void addParticipant(TransactionInfo transaction, int participant) {
        try {
            int index = tc.execute(() ->
                    l.append(new ResourceLog(transaction.id, participant))
            ).join().get();

            addIndex(transaction, index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        transaction.addParticipant(participant);
    }

    private void prepare(TransactionInfo transaction) {
        try {
            int index = tc.execute(() ->
                    l.append(new PreparingLog(transaction.id))
            ).join().get();

            addIndex(transaction, index);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        sendPrepares(transaction);
    }

    private void sendPrepares(TransactionInfo transaction) {
        for (int participant : transaction.participants) {
            tc.execute(() -> {
                c.send(participant, new PrepareComm(transaction.id));
            });
        }
    }

    private void prepared(TransactionInfo transaction, Integer from) {
        transaction.addPrepared(from);
        if (transaction.allPrepared()) {
            commit(transaction);
        }
    }

    private void commit(TransactionInfo transaction) {

        try {
            int index = tc.execute(() ->
                l.append(new CommittingLog(transaction.id))
            ).join().get();

            addIndex(transaction, index);

            sendCommits(transaction);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void sendCommits(TransactionInfo transaction) {
        for (int i : transaction.prepared) {
            tc.execute(() -> {
                c.send(i, new CommitComm(transaction.id));
            });
        }
    }

    private void committed(TransactionInfo transaction, Integer from) {
        transaction.addCommitted(from);
        if (transaction.allCommitted()) {
            try {
                tc.execute(() ->
                        l.append(new CommitLog(transaction.id))
                ).join().get();

                removeTransaction(transaction);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void rollback(TransactionInfo transaction) {
        try {
            tc.execute(() ->
                l.append(new AbortLog(transaction.id))
            ).join().get();

            for (int i : transaction.participants) {
                sendRollback(i, transaction.id);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        removeTransaction(transaction);
    }

    private void sendRollback(Integer from, int xid) {
        tc.execute(() -> {
            c.send(from, new RollbackComm(xid));
        });
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

    private void addIndex(TransactionInfo transaction, Integer index) {
        transaction.addIndex(index);
        indexesInUse.add(index);
    }

    private void twoPhaseCommitLogHandlers() {

        l.handler(PreparingLog.class, (index, p) -> {
            System.out.println("Log.PreparingLog found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o preparing ??
            }
            else {
                transaction.setPreparing();

                addIndex(transaction, index);
            }
        });

        l.handler(CommittingLog.class, (index, p) -> {
            TransactionInfo transaction = activeTransactions.get(p.xid);
            if (transaction == null){
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o committing ??
            }
            else {
                transaction.setCommitting();

                addIndex(transaction, index);
            }
        });

        l.handler(CommitLog.class, (index, p) -> {
            System.out.println("Log.CommitLog found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o commit ??
            }
            else {
                removeTransaction(transaction);
            }
        });

        l.handler(AbortLog.class, (index, p) -> {
            System.out.println("Log.AbortLog found");
            TransactionInfo transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o abort ??
            }
            else {
                removeTransaction(transaction);
            }
        });
    }

    private void twoPhaseCommitServerHandlers() {
        c.handler(OkComm.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                sendRollback(from, recv.xid);
            }
            else {
                prepared(transaction, from);
            }
        });

        c.handler(NotOkComm.class, (from, recv) ->  {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                sendRollback(from, recv.xid);
            }
            else {
                rollback(transaction);
            }
        });

        c.handler(CommitedComm.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                // Coordenador falhar depois de enviar commits, portanto vai reenvia-los
                // Nunca acontece
                // DEPENDE DA IMPLEMENTAÇÃO NO PARTICIPANTE
            }
            else {
                committed(transaction, from);
            }
        });
    }

    private void rpcLogHandlers() {

        l.handler(BeginLog.class, (index, p) -> {
            TransactionInfo transaction = new TransactionInfo(p.xid, p.client);
            activeTransactions.put(p.xid, transaction);

            addIndex(transaction, index);
        });

        l.handler(ResourceLog.class, (index, p) -> {
            TransactionInfo transaction = activeTransactions.get(p.xid);
            if (transaction == null){
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o resourcelog ??
            }
            else {
                transaction.addParticipant(p.participant);

                addIndex(transaction, index);
            }
        });
    }

    private void rpcServerHandlers() {
        c.handler(AddResourceComm.class, (from, recv) -> {

            Object r;

            TransactionInfo transaction = activeTransactions.get(recv.xid);
            if (transaction == null) {
                return Futures.completedFuture(new RollbackComm(recv.xid));
            } else {
                return Futures.completedFuture(addResource(transaction, from));
            }
        });
    }

    private void rpcClientHandlers() {
        c.handler(Begin.class, (from, recv) -> {
            int xid = begin(from);
            return Futures.completedFuture(new TransactionContext(xid));
        });

        c.handler(Commit.class, (from, recv) -> {
            TransactionInfo transaction = activeTransactions.get(recv.xContext.xid);
            if (transaction == null) {
                return Futures.completedFuture(new Rollback(recv.xContext.xid));
            }
            else {
                prepare(transaction);
                // return só depois de saber resultado final !!
                return Futures.completedFuture(null);
            }
        });
    }

    public static void main(String[] args) {

        int id = 0;

        Coordinator c = new Coordinator(id);

        c.init();
    }
}
