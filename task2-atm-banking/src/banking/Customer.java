package banking;

/**
 * The card holder. Holds the PIN check and the failed attempt counter, so the
 * lock-out rule lives with the credential instead of being re-implemented by
 * every screen that asks for a PIN.
 */
public class Customer {

    public static final int MAX_PIN_ATTEMPTS = 3;

    private final String id;
    private final String name;
    private Pin pin;
    private int failedAttempts;
    private boolean locked;

    public Customer(String id, String name, String pin) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("customer id cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("customer name cannot be empty");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.pin = new Pin(pin);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Checks the PIN and keeps score. A correct PIN clears the counter, so the
     * three attempts are three consecutive failures, not three in a lifetime.
     */
    public void authenticate(String candidate) throws InvalidPINException, CardLockedException {
        if (locked) {
            throw new CardLockedException(name);
        }
        if (pin.matches(candidate)) {
            failedAttempts = 0;
            return;
        }
        failedAttempts++;
        if (failedAttempts >= MAX_PIN_ATTEMPTS) {
            locked = true;
            throw new CardLockedException(name);
        }
        throw new InvalidPINException(MAX_PIN_ATTEMPTS - failedAttempts);
    }

    /** Changing the PIN needs the current one, even inside an open session. */
    public void changePin(String currentPin, String newPin) throws InvalidPINException, CardLockedException {
        authenticate(currentPin);
        Pin.validate(newPin);
        if (pin.matches(newPin)) {
            throw new IllegalArgumentException("the new PIN has to differ from the current one");
        }
        this.pin = new Pin(newPin);
    }

    public void unlock() {
        locked = false;
        failedAttempts = 0;
    }

    @Override
    public String toString() {
        return id + " " + name;
    }
}
