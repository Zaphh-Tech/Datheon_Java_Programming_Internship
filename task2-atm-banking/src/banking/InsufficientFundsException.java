package banking;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal or transfer would take an account past what it is
 * allowed to spend. The shortfall is carried on the exception so the caller can
 * tell the customer how much is missing instead of just saying no.
 */
public class InsufficientFundsException extends Exception {

    private final String accountNumber;
    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(String accountNumber, BigDecimal requested, BigDecimal available) {
        super("account " + accountNumber + " cannot release " + Money.format(requested)
                + ", only " + Money.format(available) + " is available (short by "
                + Money.format(requested.subtract(available)) + ")");
        this.accountNumber = accountNumber;
        this.requested = requested;
        this.available = available;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getRequested() {
        return requested;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public BigDecimal getShortfall() {
        return requested.subtract(available);
    }
}
