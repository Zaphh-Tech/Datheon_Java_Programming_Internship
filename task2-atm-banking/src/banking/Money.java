package banking;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money handling for the whole application.
 *
 * Balances are BigDecimal at two decimal places, never double. A double cannot
 * hold 0.10 exactly, so a few hundred interest postings on a double balance
 * drift away from the ledger; BigDecimal with an explicit rounding mode does
 * not. Every amount that enters the system passes through here first.
 */
public final class Money {

    public static final String CURRENCY = "INR";
    public static final int SCALE = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

    private Money() {
    }

    /** Parses typed input such as "250" or "250.75". */
    public static BigDecimal parse(String text) throws InvalidAmountException {
        if (text == null || text.isBlank()) {
            throw new InvalidAmountException("no amount entered");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new InvalidAmountException("'" + text.trim() + "' is not an amount");
        }
        if (amount.scale() > SCALE) {
            throw new InvalidAmountException("amounts cannot be finer than " + SCALE + " decimal places");
        }
        return amount.setScale(SCALE);
    }

    public static BigDecimal of(String text) {
        return new BigDecimal(text).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Rejects zero and negative amounts before they reach a balance. */
    public static BigDecimal requirePositive(BigDecimal amount) throws InvalidAmountException {
        if (amount == null) {
            throw new InvalidAmountException("no amount given");
        }
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("amount must be greater than zero, got " + format(amount));
        }
        return round(amount);
    }

    public static String format(BigDecimal amount) {
        return CURRENCY + " " + String.format("%,.2f", amount);
    }
}
