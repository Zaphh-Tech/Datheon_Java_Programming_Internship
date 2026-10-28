package library;

/** Thrown when a member id is not registered. */
public class MemberNotFoundException extends Exception {

    public MemberNotFoundException(String memberId) {
        super("no member registered as " + memberId);
    }
}
