import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import javax.sound.midi.SysexMessage;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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

        Random r = new Random();

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
        System.out.println("Log opened");

        c.handler(Prepare.class, (from, recv) -> {
            System.out.println("Preparing");
            switch (r.nextInt(3)) {
                case 0:
                case 1:
                    // ordem ??
                    tc.execute(() -> {
                        l.append(new Prepared(recv.xid));
                    });
                    tc.execute(() -> {
                        c.send(from, new Ok(recv.xid));
                    });
                    break;
                case 2:
                    // necessário ??
                    tc.execute(() -> {
                        l.append(new Abort(recv.xid));
                    });
                    tc.execute(() -> {
                        c.send(from, new NotOk(recv.xid));
                    });
                    break;
            }
        });

        c.handler(Commit.class, (from, recv) -> {
            tc.execute(() -> {
                l.append(new Commit(recv.xid));
            });
            System.out.println("Commit");
        });

        c.handler(Rollback.class, (from, recv) -> {
            tc.execute(() -> {
                l.append(new Abort(recv.xid));
            });
            System.out.println("Abort");
        });

        tc.execute(() -> c.open());
    }
}
