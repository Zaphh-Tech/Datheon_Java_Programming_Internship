package library;

/** Thrown when every copy of a title is already out on loan. */
public class BookUnavailableException extends Exception {

    public BookUnavailableException(Book book) {
        super("every copy of \"" + book.getTitle() + "\" is on loan");
    }
}
