package library;

/**
 * A catalogued title and how many of its copies are on the shelf.
 *
 * Availability is a count, not a boolean: a library with four copies of one
 * title can lend it four times, and a true/false "on loan" flag cannot express
 * that. The two copy counters only move through the lend and shelve methods.
 */
public class Book {

    private final String isbn;
    private final String title;
    private final String author;
    private final String genre;
    private final int year;
    private final int totalCopies;
    private int copiesAvailable;

    public Book(String isbn, String title, String author, String genre, int year, int totalCopies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("a book needs an ISBN");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("a book needs a title");
        }
        if (totalCopies < 1) {
            throw new IllegalArgumentException("a catalogued book needs at least one copy, got " + totalCopies);
        }
        this.isbn = isbn.trim();
        this.title = title.trim();
        this.author = author == null ? "Unknown" : author.trim();
        this.genre = genre == null ? "General" : genre.trim();
        this.year = year;
        this.totalCopies = totalCopies;
        this.copiesAvailable = totalCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getCopiesAvailable() {
        return copiesAvailable;
    }

    public int getCopiesOnLoan() {
        return totalCopies - copiesAvailable;
    }

    public boolean isAvailable() {
        return copiesAvailable > 0;
    }

    public String status() {
        if (copiesAvailable == 0) {
            return "All on loan";
        }
        if (copiesAvailable == totalCopies) {
            return "On shelf";
        }
        return "Partly out";
    }

    /** Takes one copy off the shelf. Package private: only the library lends books. */
    void lendCopy() throws BookUnavailableException {
        if (copiesAvailable == 0) {
            throw new BookUnavailableException(this);
        }
        copiesAvailable--;
    }

    void shelveCopy() {
        if (copiesAvailable >= totalCopies) {
            // Would mean more copies came back than ever went out, so the
            // ledger is wrong somewhere and silently accepting it would hide it.
            throw new IllegalStateException("all " + totalCopies + " copies of \"" + title + "\" are already shelved");
        }
        copiesAvailable++;
    }

    @Override
    public String toString() {
        return String.format("%-14s %-24s %-20s %d/%d", isbn, title, author, copiesAvailable, totalCopies);
    }
}
