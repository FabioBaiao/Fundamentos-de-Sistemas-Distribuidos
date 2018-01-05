package twophasecommit;

import common.Common;
import twophasecommit.communication.*;
import twophasecommit.logs.*;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
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
        this.c = new Clique(t, Clique.Mode.ANY, id, Common.addresses);

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
            switch (transaction.getStatus()) {
                case RUNNING:
                    tc.execute(() -> rollback(transaction));
                    break;
                case PREPARING:
                    tc.execute(() -> sendPrepares(transaction));
                    break;
                case COMMITTING:
                    tc.execute(() -> sendCommits(transaction));
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
            int index = l.append(new BeginLog(xid, client)).get();

            addIndex(transaction, index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        activeTransactions.put(xid, transaction);

        return xid;
    }

    private boolean addResource(TransactionInfo transaction, int participant, int client) {
        if (transaction.containsParticipant(participant) || transaction.getClient() != client){
            rollback(transaction);
            return false;
        }
        else {
            addParticipant(transaction, participant);
            return true;
        }
    }

    private void addParticipant(TransactionInfo transaction, int participant) {
        try {
            int index = l.append(new ResourceLog(transaction.getId(), participant)).get();

            addIndex(transaction, index);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        transaction.addParticipant(participant);
    }

    private void prepare(TransactionInfo transaction) {
        try {
            int index = l.append(new PreparingLog(transaction.getId())).get();

            addIndex(transaction, index);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        transaction.setPreparing();

        sendPrepares(transaction);
    }

    private void sendPrepares(TransactionInfo transaction) {
        for (int participant : transaction.getParticipants()) {
            c.send(participant, new PrepareComm(transaction.getId()));
        }
    }

    private void prepared(TransactionInfo transaction, Integer from) {
        transaction.addPrepared(from);
        if (transaction.allPrepared()) {
            commit(transaction);
            System.out.println("Committing");
        }
    }

    private void commit(TransactionInfo transaction) {

        try {
            int index = l.append(new CommittingLog(transaction.getId())).get();

            addIndex(transaction, index);

            transaction.setCommitting();

            sendCommits(transaction);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void sendCommits(TransactionInfo transaction) {
        for (int i : transaction.getPrepared()) {
            c.send(i, new CommitComm(transaction.getId()));
        }
    }

    private void committed(TransactionInfo transaction, Integer from) {
        transaction.addCommitted(from);
        if (transaction.allCommitted()) {
            try {
                l.append(new CommitLog(transaction.getId())).get();

                //removeTransaction(transaction);
                transaction.setCommitted();
                transaction.complete(new Commit());

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println("Committed " + System.nanoTime());
        }
    }

    private void rollback(TransactionInfo transaction) {
        try {
            l.append(new AbortLog(transaction.getId())).get();

            for (int i : transaction.getParticipants()) {
                sendRollback(i, transaction.getId());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        removeTransaction(transaction);
        transaction.complete(new Rollback());
    }

    private void sendRollback(Integer from, int xid) {
        c.send(from, new RollbackComm(xid));
    }

    private void removeTransaction(TransactionInfo transaction) {

        activeTransactions.remove(transaction.getId());
        for (int index : transaction.getIndexes()) {
            indexesInUse.remove(index);
        }
        if (indexesInUse.size() > 0) {
            l.trim(indexesInUse.first());
        }
        else {
            l.trim(Integer.MAX_VALUE);
        }
    }

    private void addIndex(TransactionInfo transaction, Integer index) {
        transaction.addIndex(index);
        indexesInUse.add(index);
    }

    private void twoPhaseCommitLogHandlers() {

        l.handler(PreparingLog.class, (index, p) -> {
            System.out.println("Log.PreparingLog found");
            TransactionInfo transaction = activeTransactions.get(p.getXid());
            if (transaction == null) {
                // quando apagou o log no final do rollback, conseguiu apagar o begin, mas não apagou o preparing ??
            }
            else {
                transaction.setPreparing();

                addIndex(transaction, index);
            }
        });

        l.handler(CommittingLog.class, (index, p) -> {
            TransactionInfo transaction = activeTransactions.get(p.getXid());
            if (transaction == null){
                // quando apagou o log no final do rollback, conseguiu apagar o begin, mas não apagou o committing ??
            }
            else {
                transaction.setCommitting();

                addIndex(transaction, index);
            }
        });

        l.handler(CommitLog.class, (index, p) -> {
            System.out.println("Log.CommitLog found");
            TransactionInfo transaction = activeTransactions.get(p.getXid());
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o commit ??
            }
            else {
                //removeTransaction(transaction);
                transaction.setCommitted();
            }
        });

        l.handler(AbortLog.class, (index, p) -> {
            System.out.println("Log.AbortLog found");
            TransactionInfo transaction = activeTransactions.get(p.getXid());
            if (transaction == null) {
                // quando apagou o log no final da transação, conseguiu apagar o begin, mas não apagou o abort ??
            }
            else {
                removeTransaction(transaction);
            }
        });
    }

    private void twoPhaseCommitServerHandlers() {
        tc.execute(() -> {
            c.handler(OkComm.class, (from, recv) -> {
                TransactionInfo transaction = activeTransactions.get(recv.getXid());
                if (transaction == null) {
                    sendRollback(from, recv.getXid());
                }
                else {
                    prepared(transaction, from);
                }
            });

            c.handler(NotOkComm.class, (from, recv) ->  {
                TransactionInfo transaction = activeTransactions.get(recv.getXid());
                if (transaction == null) {
                    sendRollback(from, recv.getXid());
                }
                else {
                    System.out.println("Rollbacking");
                    rollback(transaction);
                }
            });

            c.handler(CommitedComm.class, (from, recv) -> {
                TransactionInfo transaction = activeTransactions.get(recv.getXid());
                if (transaction == null) {
                    // Coordenador falhar depois de enviar commits, portanto vai reenvia-los
                    // Nunca acontece
                    // DEPENDE DA IMPLEMENTAÇÃO NO PARTICIPANTE
                }
                else {
                    committed(transaction, from);
                }
            });
        });
    }

    private void rpcLogHandlers() {

        l.handler(BeginLog.class, (index, p) -> {
            TransactionInfo transaction = new TransactionInfo(p.getXid(), p.getClient());
            activeTransactions.put(p.getXid(), transaction);

            addIndex(transaction, index);
        });

        l.handler(ResourceLog.class, (index, p) -> {
            TransactionInfo transaction = activeTransactions.get(p.getXid());
            if (transaction == null){
                // quando apagou o log no final do rollback, conseguiu apagar o begin, mas não apagou o resourcelog ??
            }
            else {
                transaction.addParticipant(p.getParticipant());

                addIndex(transaction, index);
            }
        });
    }

    private void rpcServerHandlers() {
        tc.execute(() -> {
            c.handler(AddResourceComm.class, (from, recv) -> {

                TransactionInfo transaction = activeTransactions.get(recv.getXid());
                if (transaction == null) {
                    return Futures.completedFuture(new RollbackComm(recv.getXid()));
                } else {
                    if (addResource(transaction, from, recv.getClient())) {
                        System.out.println("Resource added");
                        return Futures.completedFuture(new AddResourceComm());
                    }
                    else {
                        return Futures.completedFuture(new RollbackComm(recv.getXid()));
                    }
                }
            });
        });
    }

    private void rpcClientHandlers() {
        tc.execute(() -> {
            c.handler(Begin.class, (from, recv) -> {
                int xid = begin(from);
                System.out.println("Begin " + xid);
                return Futures.completedFuture(new TransactionContext(xid));
            });

            c.handler(Commit.class, (from, recv) -> {
                TransactionInfo transaction = activeTransactions.get(recv.getContext().getXid());
                if (transaction == null || transaction.getClient() != from) {
                    return Futures.completedFuture(new Rollback(recv.getContext().getXid()));
                }
                else if (transaction.getParticipants().size() == 0) {
                    try {
                        l.append(new CommitLog(transaction.getId())).get();
                        //removeTransaction(transaction);
                        transaction.setCommitted();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return Futures.completedFuture(new Commit());
                }
                else if (transaction.getStatus() == TransactionInfo.Status.PREPARING) {
                    sendPrepares(transaction);
                    return transaction.getAnswer();
                }
                else if (transaction.getStatus() == TransactionInfo.Status.COMMITTING) {
                    sendCommits(transaction);
                    return transaction.getAnswer();
                }
                else if (transaction.getStatus() == TransactionInfo.Status.COMMITTED) {
                    return Futures.completedFuture(new Commit());
                }
                else {
                    transaction.createAnswer();
                    prepare(transaction);
                    System.out.println("Preparing");
                    return transaction.getAnswer();
                }
            });
        });
    }

    public static void main(String[] args) {

        int id = 0;

        Coordinator c = new Coordinator(id);

        c.init();
    }
}
