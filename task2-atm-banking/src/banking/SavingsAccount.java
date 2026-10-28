package banking;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Savings: earns interest monthly and has to keep a minimum balance on deposit.
 * The minimum balance is the reason spendableBalance() is not the balance.
 */
public class SavingsAccount extends Account {

    public static final BigDecimal DEFAULT_MINIMUM_BALANCE = Money.of("500.00");
    public static final BigDecimal DEFAULT_ANNUAL_RATE = new BigDecimal("4.50");
    private static final int MONTHS_PER_YEAR = 12;

    private final BigDecimal annualRatePercent;
    private final BigDecimal minimumBalance;

    public SavingsAccount(String number, Customer owner, BigDecimal openingBalance) {
        this(number, owner, openingBalance, DEFAULT_ANNUAL_RATE, DEFAULT_MINIMUM_BALANCE);
    }

    public SavingsAccount(String number, Customer owner, BigDecimal openingBalance,
                          BigDecimal annualRatePercent, BigDecimal minimumBalance) {
        super(number, owner, openingBalance);
        if (annualRatePercent == null || annualRatePercent.signum() < 0) {
            throw new IllegalArgumentException("interest rate cannot be negative");
        }
        if (minimumBalance == null || minimumBalance.signum() < 0) {
            throw new IllegalArgumentException("minimum balance cannot be negative");
        }
        this.annualRatePercent = annualRatePercent;
        this.minimumBalance = Money.round(minimumBalance);
    }

    @Override
    public String kind() {
        return "Savings";
    }

    @Override
    public BigDecimal spendableBalance() {
        BigDecimal spendable = getBalance().subtract(minimumBalance);
        return spendable.signum() < 0 ? Money.ZERO : Money.round(spendable);
    }

    @Override
    public String monthEnd() {
        BigDecimal interest = monthlyInterest();
        if (interest.signum() == 0) {
            return "no interest due on " + getNumber();
        }
        post(TransactionType.INTEREST, interest, interest,
                "monthly interest at " + annualRatePercent + "% a year");
        return "credited " + Money.format(interest) + " interest to " + getNumber();
    }

    /**
     * One twelfth of the annual rate on the current balance. The division is
     * carried at six places and only the posted figure is rounded to paisa, so
     * twelve monthly postings do not systematically round in the bank's favour.
     */
    public BigDecimal monthlyInterest() {
        if (getBalance().signum() <= 0) {
            return Money.ZERO;
        }
        BigDecimal monthlyRate = annualRatePercent
                .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .divide(new BigDecimal(MONTHS_PER_YEAR), 6, RoundingMode.HALF_UP);
        return Money.round(getBalance().multiply(monthlyRate));
    }

    public BigDecimal getAnnualRatePercent() {
        return annualRatePercent;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }
}
