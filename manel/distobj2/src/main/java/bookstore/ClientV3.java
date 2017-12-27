package bookstore;

import io.atomix.catalyst.transport.Address;

public class ClientV3 {
    public static void main(String[] args) throws Exception {
        DistObj DO = new DistObj(new Address(":10001"));
        ObjRef storeRef = new ObjRef(new Address("localhost",10000),0, Store.class.getSimpleName());
        Store s = (Store) DO.objImport(storeRef);
        Book b = s.search("one");
    }
}