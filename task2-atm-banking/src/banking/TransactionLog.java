package banking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Append-only history for one account. There is no remove and no setter: the
 * only thing a caller can do is add a line and read the list back.
 */
public class TransactionLog {

    private final List<Transaction> entries = new ArrayList<>();

    void add(Transaction transaction) {
        entries.add(transaction);
    }

    public List<Transaction> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int size() {
        return entries.size();
    }

    /** The most recent entries, oldest first, for a mini statement. */
    public List<Transaction> recent(int count) {
        if (count >= entries.size()) {
            return getEntries();
        }
        return Collections.unmodifiableList(entries.subList(entries.size() - count, entries.size()));
    }
}
