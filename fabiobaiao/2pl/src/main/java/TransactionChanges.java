import java.util.ArrayList;
import java.util.List;

public class TransactionChanges {

    int xid;
    List<Object> changed;
    List<Integer> indexes;

    public TransactionChanges(int xid) {
        this.xid = xid;
        this.changed = new ArrayList<>();
        this.indexes = new ArrayList<>();
    }

    public void addIndex(int index) {
        indexes.add(index);
    }
}
