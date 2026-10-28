package banking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line of the audit trail: when it happened, what it was, how much moved
 * and what the balance became. A record because it is immutable by design,
 * which is the whole value of an audit trail.
 */
public record Transaction(LocalDateTime at,
                          TransactionType type,
                          BigDecimal amount,
                          BigDecimal balanceAfter,
                          String note) {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String timestamp() {
        return at.format(STAMP);
    }

    /** Signed from the account's point of view, so a statement can show +/-. */
    public BigDecimal signedAmount() {
        return switch (type) {
            case WITHDRAWAL, TRANSFER_OUT, FEE -> amount.negate();
            default -> amount;
        };
    }

    @Override
    public String toString() {
        return String.format("%s  %-15s %15s  %15s  %s", timestamp(), type.getLabel(),
                Money.format(signedAmount()), Money.format(balanceAfter), note == null ? "" : note);
    }
}
