package banking;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A card PIN, stored as a salted SHA-256 digest rather than as the digits.
 * Nothing in the application can read a PIN back out, which is the point: a
 * stack trace or a debug print of a Customer cannot leak it. Comparison uses
 * MessageDigest.isEqual so it does not return early on the first wrong byte.
 */
final class Pin {

    private static final int LENGTH = 4;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] salt;
    private final byte[] digest;

    Pin(String digits) {
        validate(digits);
        this.salt = new byte[16];
        RANDOM.nextBytes(this.salt);
        this.digest = hash(digits, this.salt);
    }

    boolean matches(String candidate) {
        if (candidate == null || candidate.length() != LENGTH) {
            return false;
        }
        return MessageDigest.isEqual(digest, hash(candidate, salt));
    }

    /**
     * Four digits, and not four of the same digit. The repeated-digit rule is
     * the one weak-PIN check worth having: 1111 and 0000 are the first guesses
     * anyone makes.
     */
    static void validate(String digits) {
        if (digits == null || digits.length() != LENGTH || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("a PIN is exactly " + LENGTH + " digits");
        }
        if (digits.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("a PIN cannot be the same digit four times");
        }
    }

    private static byte[] hash(String digits, byte[] salt) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(salt);
            return sha256.digest(digits.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is missing from this JVM", ex);
        }
    }
}
