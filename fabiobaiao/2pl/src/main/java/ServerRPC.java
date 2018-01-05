import Communication.*;
import Log.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class ServerRPC {

    private ThreadContext tc;
    private Clique c;
    private Log l;

    private Map<Integer, TransactionChanges> activeTransactions;

    public ServerRPC (ThreadContext tc, Clique c, int id) {
        this.tc = tc;
        this.c = c;
        this.l = new Log("" + id);
        this.activeTransactions = new HashMap<>();
    }

    private CompletableFuture<Boolean> addResource(TransactionContext xContext, int client){
        TransactionChanges transaction = activeTransactions.get(xContext.xid);
        if (transaction == null) {

            return c.sendAndReceive(0, new AddResourceComm(xContext.xid, client)).thenApply((ans) -> {

                if (ans instanceof RollbackComm) {
                    return false;
                }

                activeTransactions.put(xContext.xid, new TransactionChanges(xContext.xid, client));

                return true;
            });

        }
        else if (transaction.client != client) {
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
        Common.registerSerializers(tc);

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
                    tc.execute(() -> c.send(0, new OkComm(transaction.id)));
                    break;
            }
        }
    }

    private void rpcHandlers() {
        tc.execute(() -> {
            c.handler(PrepareComm.class, (from, recv) -> {
                TransactionChanges transaction = activeTransactions.get(recv.xid);
                if (transaction == null) {
                    // Não conhecer uma transação em que participou significa que esta deve abortar
                    // Não é necessário guardar nada no log
                    c.send(from, new NotOkComm(recv.xid));
                }
                else if (transaction.status == TransactionChanges.Status.PREPARED) {
                    c.send(from, new OkComm(recv.xid));
                }
                else {
                    try {
                        // adicionar objetos alterados
                        // adicionar objetos com lock
                        l.append(new PreparedLog(transaction.id, transaction.client, new ArrayList<>())).get();
                        transaction.status = TransactionChanges.Status.PREPARED;

                        c.send(from, new OkComm(recv.xid));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            c.handler(CommitComm.class, (from, recv) -> {
                if (activeTransactions.containsKey(recv.xid)) {
                    try {
                        TransactionChanges transaction = activeTransactions.get(recv.xid);
                        l.append(new CommitLog(recv.xid)).get();

                        c.send(from, new CommitedComm(recv.xid));
                        // libertar locks

                        activeTransactions.remove(transaction.id);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // Commit já foi feito
                    c.send(from, new CommitedComm(recv.xid));
                }
            });

            c.handler(RollbackComm.class, (from, recv) -> {
                if (activeTransactions.containsKey(recv.xid)) {
                    try {
                        l.append(new AbortLog(recv.xid)).get();

                        // recover initial status
                        // release locks of transaction
                        activeTransactions.remove(recv.xid);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void logHandlers() {
        l.handler(PreparedLog.class, (index, p) -> {
            TransactionChanges transaction = new TransactionChanges(p.xid, p.client);
            activeTransactions.put(p.xid, transaction);
            transaction.status = TransactionChanges.Status.PREPARED;
            // guardar estado inicial das variaveis alteradas
            // repor variaveis alteradas
        });


        l.handler(CommitLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // Não existir Prepared. Possível ??
            } else {
                tc.execute(() -> {
                    c.send(0, new CommitedComm(p.xid));
                });
                // libertar locks
                activeTransactions.remove(transaction.id);
            }
        });

        l.handler(AbortLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // Não existir Prepared. Possível ??
            } else {
                // recover initial status
                // release locks of transaction
                activeTransactions.remove(transaction.id);
            }
        });
    }
}
