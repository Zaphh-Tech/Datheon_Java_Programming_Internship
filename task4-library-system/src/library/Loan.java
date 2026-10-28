package library;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One copy of one book, in the hands of one member, from a date until it comes
 * back. The fine is derived from the dates rather than stored, so it cannot
 * fall out of step with them.
 */
public class Loan {

    public static final int LOAN_DAYS = 14;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    /** A fine stops growing here. Past this point the branch writes the book off. */
    public static final BigDecimal MAX_FINE = new BigDecimal("250.00");

    private final Book book;
    private final Member member;
    private final LocalDate checkedOut;
    private final LocalDate dueOn;
    private LocalDate returnedOn;

    Loan(Book book, Member member, LocalDate checkedOut) {
        this.book = book;
        this.member = member;
        this.checkedOut = checkedOut;
        this.dueOn = checkedOut.plusDays(LOAN_DAYS);
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getCheckedOut() {
        return checkedOut;
    }

    public LocalDate getDueOn() {
        return dueOn;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public boolean isOpen() {
        return returnedOn == null;
    }

    /**
     * Days past the due date. Counted against the return date once the book is
     * back, and against the given date while it is still out, which is why
     * every caller has to say which date it is asking about.
     */
    public long daysOverdue(LocalDate asOf) {
        LocalDate reference = returnedOn != null ? returnedOn : asOf;
        if (!reference.isAfter(dueOn)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueOn, reference);
    }

    public boolean isOverdue(LocalDate asOf) {
        return daysOverdue(asOf) > 0;
    }

    public BigDecimal fine(LocalDate asOf) {
        long late = daysOverdue(asOf);
        if (late == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(late)).setScale(2);
        return fine.min(MAX_FINE).setScale(2);
    }

    /** Days left before it is due, negative once it is late. */
    public long daysRemaining(LocalDate asOf) {
        return ChronoUnit.DAYS.between(asOf, dueOn);
    }

    void markReturned(LocalDate date) {
        this.returnedOn = date;
    }

    @Override
    public String toString() {
        return String.format("%-14s %-24s %-14s %s", book.getIsbn(), book.getTitle(), member.getId(), dueOn);
    }
}
