package banking;

/**
 * Thrown on a failed PIN check. The message deliberately says nothing about
 * which part was wrong and never echoes the digits that were entered.
 */
public class InvalidPINException extends Exception {

    private final int attemptsLeft;

    public InvalidPINException(int attemptsLeft) {
        super("incorrect PIN, " + attemptsLeft + " attempt(s) left before the card is retained");
        this.attemptsLeft = attemptsLeft;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }
}
