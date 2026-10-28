package banking;

/** Thrown when a card has already been locked by too many failed PIN attempts. */
public class CardLockedException extends Exception {

    public CardLockedException(String customerName) {
        super("the card for " + customerName + " is locked, the branch has to reset it");
    }
}
