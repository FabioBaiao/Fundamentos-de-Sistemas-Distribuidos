import Communication.*;
import Log.*;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    public boolean register(TransactionContext xContext, int client){
        TransactionChanges transaction = activeTransactions.get(xContext.xid);
        if (transaction == null) {
            try {
                Object o = tc.execute(() -> c.sendAndReceive(0, new AddResourceComm(xContext.xid))).join().get();

                if (o instanceof RollbackComm) {
                    return false;
                }

                transaction = new TransactionChanges(xContext.xid, client);
                activeTransactions.put(xContext.xid, transaction);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        else if (transaction.client != client) {
            return false;
        }

        return true;
    }

    public void init() {
        Common.registerSerializers(tc);

        logHandlers();
        rpcHandlers();

        try {
            tc.execute(() -> c.open()).join().get();
            tc.execute(() -> l.open()).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void rpcHandlers() {
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
                            l.append(new PreparedLog(transaction.id, transaction.client, new ArrayList<>()))
                    ).join().get();


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
    }

    private void logHandlers() {
        l.handler(PreparedLog.class, (index, p) -> {
            TransactionChanges transaction = new TransactionChanges(p.xid, p.client);
            activeTransactions.put(p.xid, transaction);
        });


        l.handler(CommitLog.class, (index, p) -> {
            TransactionChanges transaction = activeTransactions.get(p.xid);
            if (transaction == null) {
                // Não existir Prepared. Possível ??
            } else {
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
                // Não existir Prepared. Possível ??
            } else {
                // recover initial status
                // release locks of transaction
                // terminar transação
            }
        });
    }
}
