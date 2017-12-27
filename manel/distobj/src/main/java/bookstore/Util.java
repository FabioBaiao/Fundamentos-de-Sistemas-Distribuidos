package bookstore;

import io.atomix.catalyst.concurrent.ThreadContext;

public class Util {
    public static Object makeRemote(ThreadContext tc, ObjRef ref) throws Exception{
        if(ref.cls.equals(Book.class.getSimpleName())){
            System.out.println("here");
            return new RemoteBook(tc, ref.id, ref.address);
        }else if(ref.cls.equals(Cart.class.getSimpleName())) {
            return new RemoteCart(ref.id, ref.address);
        }
        return null;
    }
}
