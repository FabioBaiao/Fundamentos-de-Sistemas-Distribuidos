import java.util.ArrayList;
import java.util.List;

public class TransactionChanges {

    int id;
    List<Object> changed;
    public int client;

    Status status;

    public TransactionChanges(int id, int client) {
        this.id = id;
        this.client = client;
        this.changed = new ArrayList<>();
    }

    public enum Status {
        PREPARED
    }
}
