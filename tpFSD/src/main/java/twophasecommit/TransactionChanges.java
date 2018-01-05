package twophasecommit;

import java.util.ArrayList;
import java.util.List;

public class TransactionChanges {

    private int id;
    private List<Object> changed;
    private int client;

    Status status;

    public TransactionChanges(int id, int client) {
        this.id = id;
        this.client = client;
        this.changed = new ArrayList<>();
    }

    public int getClient() {
        return client;
    }

    public int getId() {
        return id;
    }

    public enum Status {
        PREPARED
    }
}
