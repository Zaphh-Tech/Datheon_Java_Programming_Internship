package banking;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Current account: no interest earned, but it may be overdrawn up to an agreed
 * limit. Going below zero is legitimate here, so the balance is allowed to be
 * negative and spendableBalance() adds the overdraft on top.
 */
public class CheckingAccount extends Account {

    public static final BigDecimal DEFAULT_OVERDRAFT_LIMIT = Money.of("2000.00");
    public static final BigDecimal OVERDRAFT_MONTHLY_RATE_PERCENT = new BigDecimal("1.25");

    private final BigDecimal overdraftLimit;

    public CheckingAccount(String number, Customer owner, BigDecimal openingBalance) {
        this(number, owner, openingBalance, DEFAULT_OVERDRAFT_LIMIT);
    }

    public CheckingAccount(String number, Customer owner, BigDecimal openingBalance, BigDecimal overdraftLimit) {
        super(number, owner, openingBalance);
        if (overdraftLimit == null || overdraftLimit.signum() < 0) {
            throw new IllegalArgumentException("overdraft limit cannot be negative");
        }
        this.overdraftLimit = Money.round(overdraftLimit);
    }

    @Override
    public String kind() {
        return "Current";
    }

    @Override
    public BigDecimal spendableBalance() {
        return Money.round(getBalance().add(overdraftLimit));
    }

    @Override
    public String monthEnd() {
        BigDecimal overdrawn = overdrawnAmount();
        if (overdrawn.signum() == 0) {
            return "no overdraft charge on " + getNumber();
        }
        BigDecimal charge = Money.round(overdrawn
                .multiply(OVERDRAFT_MONTHLY_RATE_PERCENT)
                .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
        post(TransactionType.FEE, charge, charge.negate(),
                "overdraft charge at " + OVERDRAFT_MONTHLY_RATE_PERCENT + "% a month");
        return "charged " + Money.format(charge) + " overdraft interest on " + getNumber();
    }

    /** How far below zero the account currently sits, zero if it is in credit. */
    public BigDecimal overdrawnAmount() {
        return getBalance().signum() < 0 ? getBalance().negate() : Money.ZERO;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}
