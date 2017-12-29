import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionInfo {

    private int client;
    Set<Integer> participants;

    Set<Integer> prepared;
    public List<Integer> indexes;
    public int id;

    Status status;

    public TransactionInfo() {
        this.participants = new HashSet<>();
        this.prepared = new HashSet<>();
    }

    public TransactionInfo(int id) {
        this.id = id;
        this.participants = new HashSet<>();
        this.prepared = new HashSet<>();
    }

    public TransactionInfo(int id, List<Integer> participants) {
        this.id = id;
        this.participants = participants;
        this.prepared = new HashSet<>();
    }

    public TransactionInfo(int xid, int client) {
        this.id = xid;
        this.client = client;
        this.status = Status.RUNNING;
        this.participants = new HashSet<>();
        this.prepared = new HashSet<>();
        this.indexes = new HashSet<>();
    }

    public boolean allPrepared() {
        return prepared.size() == participants.size();
    }


    public void addPrepared(Integer from) {
        this.prepared.add(from);
    }

    public void addParticipant(int i) {
        this.participants.add(i);
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public void addIndex(Integer index) {
        this.indexes.add(index);
    }

    public void setPreparing() {
        this.status = Status.PREPARING;
    }

    public boolean isPreparing() {
        return this.status == Status.PREPARING;
    }


    public enum Status {
        RUNNING, PREPARING
    }
}
