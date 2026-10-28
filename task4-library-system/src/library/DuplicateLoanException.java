package library;

/** Thrown when a member tries to borrow a second copy of a title they already have out. */
public class DuplicateLoanException extends Exception {

    public DuplicateLoanException(Member member, Book book) {
        super(member.getName() + " already has \"" + book.getTitle() + "\" out");
    }
}
