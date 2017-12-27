package bookstore;

import io.atomix.catalyst.transport.Address;

public class Server {
    public static void main(String[] args) throws Exception {
        DistObj DO = new DistObj(new Address(":10000"));
        LocalStore store = new LocalStore();
        DO.objExport(store);
    }
}
