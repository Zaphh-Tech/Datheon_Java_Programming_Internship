package library;

/** Thrown when a return is attempted for a book the member does not have out. */
public class LoanNotFoundException extends Exception {

    public LoanNotFoundException(String memberId, String isbn) {
        super("member " + memberId + " has no open loan for ISBN " + isbn);
    }
}
