package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteStore implements Store {

    DistributedObjectsRuntime dor;

    Address a;

    private int id = 1;

    public RemoteStore(DistributedObjectsRuntime dor, Address a,  int id){
        this.dor = dor;
        this.a = a;
        this.id = id;
    }

    public Book search(String title)  {
        try {
            StoreSearchRep r = (StoreSearchRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreSearchReq(id, title))
            ).join().get();
            return (Book) dor.objImport(r.ref);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cart newCart() {
        try {
            StoreMakeCartRep r = (StoreMakeCartRep) dor.tc.execute(() ->
                    dor.cons.get(a).sendAndReceive(new StoreMakeCartReq())
            ).join().get();
            return (Cart) dor.objImport(r.ref);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
