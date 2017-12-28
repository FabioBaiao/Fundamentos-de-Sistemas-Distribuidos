import java.util.List;

public class TransactionChanges {

    int xid;
    List<Object> changed;

    public TransactionChanges(int xid) {
        this.xid = xid;
    }
}
