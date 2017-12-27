import java.util.ArrayList;
import java.util.List;

public class TransactionInfo {

    private int client;
    List<Integer> participants;

    List<Integer> prepared;
    public List<Integer> indexes;
    public int id;

    Status status;

    public TransactionInfo() {
        this.participants = new ArrayList<>();
        this.prepared = new ArrayList<>();
    }

    public TransactionInfo(int id) {
        this.id = id;
        this.participants = new ArrayList<>();
        this.prepared = new ArrayList<>();
    }

    public TransactionInfo(int id, List<Integer> participants) {
        this.id = id;
        this.participants = participants;
        this.prepared = new ArrayList<>();
    }

    public TransactionInfo(int xid, int client) {
        this.id = xid;
        this.client = client;
        this.status = Status.RUNNING;
        this.participants = new ArrayList<>();
        this.prepared = new ArrayList<>();
        this.indexes = new ArrayList<>();
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
