package library;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A library member and their borrowing history. Returned loans stay on the
 * list: the history is what tells the desk whether someone keeps bringing
 * books back late.
 */
public class Member {

    public static final int LOAN_LIMIT = 3;

    private final String id;
    private final String name;
    private final LocalDate joined;
    private final List<Loan> loans = new ArrayList<>();
    private BigDecimal finesPaid = BigDecimal.ZERO.setScale(2);

    public Member(String id, String name, LocalDate joined) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("a member needs an id");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a member needs a name");
        }
        this.id = id.trim().toUpperCase();
        this.name = name.trim();
        this.joined = joined;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getJoined() {
        return joined;
    }

    public List<Loan> getLoans() {
        return Collections.unmodifiableList(loans);
    }

    public List<Loan> activeLoans() {
        List<Loan> open = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOpen()) {
                open.add(loan);
            }
        }
        return open;
    }

    public boolean canBorrow() {
        return activeLoans().size() < LOAN_LIMIT;
    }

    public boolean hasOpenLoanFor(String isbn) {
        for (Loan loan : activeLoans()) {
            if (loan.getBook().getIsbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }

    public Loan openLoanFor(String isbn) {
        for (Loan loan : activeLoans()) {
            if (loan.getBook().getIsbn().equals(isbn)) {
                return loan;
            }
        }
        return null;
    }

    public BigDecimal getFinesPaid() {
        return finesPaid;
    }

    /** Fines running on books that are still out, as at the given date. */
    public BigDecimal finesOutstanding(LocalDate asOf) {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Loan loan : activeLoans()) {
            total = total.add(loan.fine(asOf));
        }
        return total;
    }

    void addLoan(Loan loan) {
        loans.add(loan);
    }

    void recordFinePaid(BigDecimal amount) {
        finesPaid = finesPaid.add(amount).setScale(2);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-22s joined %s, %d book(s) out", id, name, joined, activeLoans().size());
    }
}
