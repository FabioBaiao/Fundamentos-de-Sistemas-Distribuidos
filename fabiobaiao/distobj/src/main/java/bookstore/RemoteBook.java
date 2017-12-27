package bookstore;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;

public class RemoteBook implements Book {

    DistributedObjectsRuntime dor;

    Address a;

    int id;

    RemoteBook(DistributedObjectsRuntime dor, Address a, int id) {
        this.dor = dor;
        this.a = a;
        this.id = id;
    }

    @Override
    public String getTitle() {
        // ...
        return null;
    }
}
