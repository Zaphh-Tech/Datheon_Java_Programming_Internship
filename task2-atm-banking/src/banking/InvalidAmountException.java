package banking;

/** Thrown when an amount is not a usable sum of money: zero, negative, or finer than one paisa. */
public class InvalidAmountException extends Exception {

    public InvalidAmountException(String message) {
        super(message);
    }
}
