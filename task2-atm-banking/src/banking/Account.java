package banking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Shared behaviour of every account: a number, an owner, a balance and a log.
 *
 * The balance is private and only moves through post(), so no subclass or
 * caller can change a balance without writing the matching audit line. What a
 * subclass decides is how much of the balance may actually be spent
 * (spendableBalance) and what happens at month end (monthEnd).
 */
public abstract class Account {

    private final String number;
    private final Customer owner;
    private final TransactionLog log = new TransactionLog();
    private BigDecimal balance;

    protected Account(String number, Customer owner, BigDecimal openingBalance) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("account number cannot be empty");
        }
        if (owner == null) {
            throw new IllegalArgumentException("an account needs an owner");
        }
        if (openingBalance == null || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("opening balance cannot be negative");
        }
        this.number = number.trim();
        this.owner = owner;
        this.balance = Money.round(openingBalance);
        log.add(new Transaction(LocalDateTime.now(), TransactionType.OPENING, this.balance, this.balance,
                "account opened"));
    }

    public String getNumber() {
        return number;
    }

    public Customer getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public TransactionLog getLog() {
        return log;
    }

    /** Human readable account type, used on statements. */
    public abstract String kind();

    /** How much the customer can take out right now, which is not always the balance. */
    public abstract BigDecimal spendableBalance();

    /** Month end posting: interest on a savings account, overdraft charges on a current account. */
    public abstract String monthEnd();

    public void deposit(BigDecimal amount, String note) throws InvalidAmountException {
        BigDecimal value = Money.requirePositive(amount);
        post(TransactionType.DEPOSIT, value, value, note);
    }

    /**
     * Takes money out, or refuses. The check is against spendableBalance() so a
     * current account can go negative up to its overdraft while a savings
     * account cannot touch its minimum balance.
     */
    public void withdraw(BigDecimal amount, String note)
            throws InvalidAmountException, InsufficientFundsException {
        BigDecimal value = Money.requirePositive(amount);
        BigDecimal spendable = spendableBalance();
        if (value.compareTo(spendable) > 0) {
            throw new InsufficientFundsException(number, value, spendable);
        }
        post(TransactionType.WITHDRAWAL, value, value.negate(), note);
    }

    /**
     * The one place a balance changes. Bank initiated postings (interest, fees,
     * the two halves of a transfer) come through here as well, which is why the
     * limit check sits in withdraw() and not in this method.
     */
    protected void post(TransactionType type, BigDecimal amount, BigDecimal delta, String note) {
        balance = Money.round(balance.add(delta));
        log.add(new Transaction(LocalDateTime.now(), type, Money.round(amount.abs()), balance, note));
    }

    /**
     * The two halves of a transfer. Package private on purpose: a transfer has
     * to go through Bank so that both legs are posted, and nothing outside this
     * package can post only one of them.
     */
    void transferOut(BigDecimal amount, Account destination)
            throws InvalidAmountException, InsufficientFundsException {
        BigDecimal value = Money.requirePositive(amount);
        BigDecimal spendable = spendableBalance();
        if (value.compareTo(spendable) > 0) {
            throw new InsufficientFundsException(number, value, spendable);
        }
        post(TransactionType.TRANSFER_OUT, value, value.negate(),
                "to " + destination.getNumber() + " " + destination.getOwner().getName());
    }

    void transferIn(BigDecimal amount, Account source) throws InvalidAmountException {
        BigDecimal value = Money.requirePositive(amount);
        post(TransactionType.TRANSFER_IN, value, value,
                "from " + source.getNumber() + " " + source.getOwner().getName());
    }

    @Override
    public String toString() {
        return String.format("%-8s %-16s %-18s %14s", number, kind(), owner.getName(), Money.format(balance));
    }
}
