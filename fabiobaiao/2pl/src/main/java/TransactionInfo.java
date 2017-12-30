import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionInfo {

    Set<Integer> participants;
    Set<Integer> prepared;
    Set<Integer> committed;
    public List<Integer> indexes;
    public int id;

    Status status;

    public TransactionInfo() {
    }

    public TransactionInfo(int id) {
        this.id = id;
        status = Status.RUNNING;
        this.participants = new HashSet<>();
        this.prepared = new HashSet<>();
        this.committed = new HashSet<>();
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

    public void addIndex(Integer index) {
        this.indexes.add(index);
    }

    public void setPreparing() {
        this.status = Status.PREPARING;
    }

    public boolean isPreparing() {
        return this.status == Status.PREPARING;
    }

    public void addCommitted(int from) {
        this.committed.add(from);
    }

    public boolean allCommitted() {
        return participants.size() == committed.size();
    }

    public boolean containsParticipant(Integer from) {
        return participants.contains(from);
    }

    public void setCommitting() {
        this.status = Status.COMMITTING;
    }


    public enum Status {
        RUNNING, PREPARING, COMMITTING
    }
}
