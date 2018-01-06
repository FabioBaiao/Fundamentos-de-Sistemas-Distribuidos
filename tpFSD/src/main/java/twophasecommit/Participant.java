package twophasecommit;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import twophasecommit.communication.*;
import twophasecommit.logs.AbortLog;
import twophasecommit.logs.CommitLog;
import twophasecommit.logs.PreparedLog;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class Participant {

    private ThreadContext tc;
    private Clique c;
    private Log l;

    private Map<Integer, TransactionChanges> activeTransactions;

    public Participant(ThreadContext tc, Clique c, int id) {
        this.tc = tc;
        this.c = c;
        this.l = new Log("" + id);
        this.activeTransactions = new HashMap<>();

        this.changes = new HashMap<>();
    }

    private CompletableFuture<Boolean> addResource(TransactionContext xContext, int client){
        TransactionChanges transaction = activeTransactions.get(xContext.getXid());
        if (transaction == null) {

            return c.sendAndReceive(0, new AddResourceComm(xContext.getXid(), client)).thenApply((ans) -> {

                if (ans instanceof RollbackComm) {
                    return false;
                }

                activeTransactions.put(xContext.getXid(), new TransactionChanges(xContext.getXid(), client));

                return true;
            });

        }
        else if (transaction.getClient() != client) {
            return Futures.completedFuture(false);
        }

        return Futures.completedFuture(true);
    }

    public <Object> CompletableFuture<Object> register(TransactionContext xContext, Integer from, Supplier<Object> c) {
        return addResource(xContext, from).thenApply((res) -> {
            if (res == false) {
                return (Object) new Rollback();
            }
            else {
                return c.get();
            }
        });
    }

    public void init() {
        //common.Common.registerSerializers(tc);

        logHandlers();
        rpcHandlers();

        try {
            tc.execute(() -> l.open()).join().get();
            tc.execute(() -> c.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (TransactionChanges transaction : activeTransactions.values()) {
            switch (transaction.status) {
                case PREPARED:
                    tc.execute(() -> c.send(0, new OkComm(transaction.getId())));
                    break;
            }
        }
    }

    private void rpcHandlers() {
        tc.execute(() -> {
            c.handler(PrepareComm.class, (from, recv) -> {
                TransactionChanges transaction = activeTransactions.get(recv.getXid());
                if (transaction == null) {
                    // Não conhecer uma transação em que participou significa que esta deve abortar
                    // Não é necessário guardar nada no log
                    c.send(from, new NotOkComm(recv.getXid()));
                }
                else if (transaction.status == TransactionChanges.Status.PREPARED) {
                    c.send(from, new OkComm(recv.getXid()));
                }
                else {
                    try {
                        // adicionar objetos alterados
                        // adicionar objetos com lock
                        l.append(new PreparedLog(transaction.getId(), transaction.getClient(), new ArrayList<>())).get();
                        transaction.status = TransactionChanges.Status.PREPARED;

                        c.send(from, new OkComm(recv.getXid()));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            c.handler(CommitComm.class, (from, recv) -> {
                if (activeTransactions.containsKey(recv.getXid())) {
                    try {
                        TransactionChanges transaction = activeTransactions.get(recv.getXid());
                        l.append(new CommitLog(recv.getXid())).get();

                        c.send(from, new CommitedComm(recv.getXid()));
                        release(recv.getXid());

                        activeTransactions.remove(transaction.getId());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // Commit já foi feito
                    c.send(from, new CommitedComm(recv.getXid()));
                }
            });

            c.handler(RollbackComm.class, (from, recv) -> {
                if (activeTransactions.containsKey(recv.getXid())) {
                    try {
                        l.append(new AbortLog(recv.getXid())).get();

                        // recover initial status
                        release(recv.getXid());
                        activeTransactions.remove(recv.getXid());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void logHandlers() {
        l.handler(PreparedLog.class, (index, p) -> {
            TransactionChanges transaction = new TransactionChanges(p.getXid(), p.getClient());
            activeTransactions.put(p.getXid(), transaction);
            transaction.status = TransactionChanges.Status.PREPARED;
            // guardar estado inicial das variaveis alteradas
            // repor variaveis alteradas
        });


        l.handler(CommitLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.getXid());
            if (transaction == null) {
                // Não existir Prepared. Possível ??
            } else {
                tc.execute(() -> {
                    c.send(0, new CommitedComm(p.getXid()));
                });
                // libertar locks
                activeTransactions.remove(transaction.getId());
            }
        });

        l.handler(AbortLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.getXid());
            if (transaction == null) {
                // Não existir Prepared. Possível ??
            } else {
                // recover initial status
                // release locks of transaction
                activeTransactions.remove(transaction.getId());
            }
        });
    }

    Map<Object, LockData> changes;

    public CompletableFuture<Void> acquire(TransactionContext xContext, Object o) {
        LockData lock = changes.get(o);
        if (lock == null) {
            lock = new LockData(xContext.getXid());
            activeTransactions.get(xContext.getXid()).addChanged(o);
            changes.put(o, lock);
        }

        return lock.acquire(xContext.getXid());
    }

    public void release(int xid) {
        TransactionChanges transaction = activeTransactions.get(xid);
        for (Object o : transaction.getChanged()) {
            LockData lock = changes.get(o);
            if(lock.release()) {
                changes.remove(o);
            }
        }
    }

    private class LockData {

        int xid;
        Queue<Wait> waiting;

        LockData(int xid) {
            this.xid = xid;
            this.waiting = new LinkedList<>();
        }

        public int getXid() {
            return xid;
        }

        public CompletableFuture<Void> acquire(int xid) {
            CompletableFuture<Void> acquired = new CompletableFuture<>();
            if (this.xid == xid)
                acquired.complete(null);
            else
                waiting.add(new Wait(acquired, xid));

            return acquired;

        }

        public boolean release() {
            if (waiting.size() == 0) {
                return true;
            }
            else {
                Wait w = waiting.remove();
                xid = w.xid;
                w.compFuture.complete(null);
                return false;
            }
        }

        private class Wait {
            CompletableFuture<Void> compFuture;
            int xid;

            Wait(CompletableFuture<Void> compFuture, int xid) {
                this.compFuture = compFuture;
                this.xid = xid;
            }
        }
    }
}
