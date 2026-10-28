package library;

/** Thrown when an ISBN is not in the catalogue. */
public class BookNotFoundException extends Exception {

    public BookNotFoundException(String isbn) {
        super("no book catalogued under ISBN " + isbn);
    }
}
