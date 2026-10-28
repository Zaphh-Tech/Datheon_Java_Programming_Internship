package library;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Library administration: catalogue, members, loans, due dates and fines.
 *
 * The catalogue is a Map keyed by ISBN because every operation starts from an
 * ISBN, and loans are a list in the order they were taken out. Dates run off a
 * working date held in this class rather than LocalDate.now(), so the desk can
 * back-date an entry and so overdue behaviour can be demonstrated without
 * waiting two weeks.
 *
 * Usage:
 *   java library.LibrarySystem [catalogue.csv]
 */
public class LibrarySystem {

    private static final String DEFAULT_CSV = "../data/books_catalog.csv";

    private final Map<String, Book> catalogue = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private final Scanner input = new Scanner(System.in);
    private LocalDate workingDate = LocalDate.now();

    public static void main(String[] args) {
        LibrarySystem library = new LibrarySystem();
        library.loadCatalogue(Path.of(args.length > 0 ? args[0] : DEFAULT_CSV));

        // Two members so the desk has someone to lend to on a fresh start.
        library.registerMember("M-01", "Aarav Sharma");
        library.registerMember("M-02", "Diya Patel");

        library.run();
    }

    // Catalogue and members

    public void addBook(Book book) {
        if (catalogue.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN " + book.getIsbn() + " is already catalogued");
        }
        catalogue.put(book.getIsbn(), book);
    }

    public Member registerMember(String id, String name) {
        Member member = new Member(id, name, workingDate);
        if (members.containsKey(member.getId())) {
            throw new IllegalArgumentException("member " + member.getId() + " is already registered");
        }
        members.put(member.getId(), member);
        return member;
    }

    public Book book(String isbn) throws BookNotFoundException {
        Book book = catalogue.get(isbn == null ? "" : isbn.trim());
        if (book == null) {
            throw new BookNotFoundException(isbn);
        }
        return book;
    }

    public Member member(String id) throws MemberNotFoundException {
        Member member = members.get(id == null ? "" : id.trim().toUpperCase());
        if (member == null) {
            throw new MemberNotFoundException(id);
        }
        return member;
    }

    public Collection<Book> getCatalogue() {
        return catalogue.values();
    }

    public Collection<Member> getMembers() {
        return members.values();
    }

    public LocalDate getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(LocalDate date) {
        this.workingDate = date;
    }

    // Loans

    /**
     * Lends one copy. The copy comes off the shelf first: if the book is
     * unavailable that call throws and no loan record is created, so the shelf
     * count and the loan list cannot disagree.
     */
    public Loan checkOut(String isbn, String memberId, LocalDate on)
            throws BookNotFoundException, MemberNotFoundException, BookUnavailableException,
            LoanLimitException, DuplicateLoanException {
        Book book = book(isbn);
        Member member = member(memberId);
        if (!member.canBorrow()) {
            throw new LoanLimitException(member, Member.LOAN_LIMIT);
        }
        if (member.hasOpenLoanFor(book.getIsbn())) {
            throw new DuplicateLoanException(member, book);
        }
        book.lendCopy();
        Loan loan = new Loan(book, member, on);
        member.addLoan(loan);
        loans.add(loan);
        return loan;
    }

    /** Takes a book back, shelves the copy and returns the fine that was charged. */
    public BigDecimal returnBook(String isbn, String memberId, LocalDate on)
            throws BookNotFoundException, MemberNotFoundException, LoanNotFoundException {
        Book book = book(isbn);
        Member member = member(memberId);
        Loan loan = member.openLoanFor(book.getIsbn());
        if (loan == null) {
            throw new LoanNotFoundException(member.getId(), book.getIsbn());
        }
        if (on.isBefore(loan.getCheckedOut())) {
            throw new IllegalArgumentException("a book cannot come back on " + on
                    + ", before it was taken out on " + loan.getCheckedOut());
        }
        loan.markReturned(on);
        book.shelveCopy();
        BigDecimal fine = loan.fine(on);
        if (fine.signum() > 0) {
            member.recordFinePaid(fine);
        }
        return fine;
    }

    public List<Loan> openLoans() {
        List<Loan> open = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOpen()) {
                open.add(loan);
            }
        }
        return open;
    }

    /** Open loans that are past their due date, most overdue first. */
    public List<Loan> overdueLoans(LocalDate asOf) {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : openLoans()) {
            if (loan.isOverdue(asOf)) {
                overdue.add(loan);
            }
        }
        overdue.sort((left, right) -> Long.compare(right.daysOverdue(asOf), left.daysOverdue(asOf)));
        return overdue;
    }

    public BigDecimal finesOutstanding(LocalDate asOf) {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Loan loan : openLoans()) {
            total = total.add(loan.fine(asOf));
        }
        return total;
    }

    // Searching

    public List<Book> searchByTitle(String fragment) {
        List<Book> found = new ArrayList<>();
        String needle = fragment.trim().toLowerCase();
        for (Book book : catalogue.values()) {
            if (book.getTitle().toLowerCase().contains(needle)) {
                found.add(book);
            }
        }
        return found;
    }

    public List<Book> searchByAuthor(String fragment) {
        List<Book> found = new ArrayList<>();
        String needle = fragment.trim().toLowerCase();
        for (Book book : catalogue.values()) {
            if (book.getAuthor().toLowerCase().contains(needle)) {
                found.add(book);
            }
        }
        return found;
    }

    public List<Book> availableBooks() {
        List<Book> found = new ArrayList<>();
        for (Book book : catalogue.values()) {
            if (book.isAvailable()) {
                found.add(book);
            }
        }
        return found;
    }

    public void loadCatalogue(Path file) {
        try {
            int added = 0;
            int skipped = 0;
            for (Book book : BookCsvReader.read(file)) {
                if (catalogue.containsKey(book.getIsbn())) {
                    skipped++;
                } else {
                    catalogue.put(book.getIsbn(), book);
                    added++;
                }
            }
            System.out.printf("Loaded %d title(s) from %s", added, file);
            if (skipped > 0) {
                System.out.printf(", skipped %d ISBN(s) already catalogued", skipped);
            }
            System.out.println(".");
        } catch (IOException ex) {
            System.out.println("Could not read the catalogue: " + ex.getMessage());
        }
    }

    // Console

    public void run() {
        System.out.println("Library Management System");
        System.out.println("Working date is " + workingDate + ". Option 9 changes it.");
        while (true) {
            printMenu();
            String choice = readLine("Choice: ");
            if (choice == null) {
                System.out.println();
                System.out.println("Input ended, closing the desk.");
                return;
            }
            switch (choice.trim()) {
                case "1" -> LibraryPrinter.printCatalogue(catalogue.values(), "CATALOGUE");
                case "2" -> search();
                case "3" -> registerMemberFromPrompt();
                case "4" -> LibraryPrinter.printMembers(members.values(), workingDate);
                case "5" -> checkOutFromPrompt();
                case "6" -> returnFromPrompt();
                case "7" -> LibraryPrinter.printLoans(openLoans(), workingDate, "OPEN LOANS");
                case "8" -> overdueReport();
                case "9" -> changeWorkingDate();
                case "10" -> loadCatalogueFromPrompt();
                case "0" -> {
                    System.out.printf("Closing with %d title(s), %d member(s) and %d open loan(s).%n",
                            catalogue.size(), members.size(), openLoans().size());
                    return;
                }
                case "" -> { }
                default -> System.out.println("Unknown option: " + choice);
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1) Show catalogue");
        System.out.println("2) Search the catalogue");
        System.out.println("3) Register a member");
        System.out.println("4) Show members");
        System.out.println("5) Check out a book");
        System.out.println("6) Return a book");
        System.out.println("7) Open loans");
        System.out.println("8) Overdue report");
        System.out.println("9) Set the working date (now " + workingDate + ")");
        System.out.println("10) Load catalogue from CSV");
        System.out.println("0) Exit");
    }

    private void search() {
        System.out.println("Search by: 1) title  2) author  3) what is on the shelf");
        String how = readLine("Choice: ");
        if (how == null) {
            return;
        }
        switch (how.trim()) {
            case "1" -> {
                String fragment = readLine("Title contains: ");
                if (fragment != null) {
                    LibraryPrinter.printCatalogue(searchByTitle(fragment), "TITLES MATCHING \"" + fragment.trim() + "\"");
                }
            }
            case "2" -> {
                String fragment = readLine("Author contains: ");
                if (fragment != null) {
                    LibraryPrinter.printCatalogue(searchByAuthor(fragment), "BOOKS BY \"" + fragment.trim() + "\"");
                }
            }
            case "3" -> LibraryPrinter.printCatalogue(availableBooks(), "ON THE SHELF");
            default -> System.out.println("Unknown option: " + how);
        }
    }

    private void registerMemberFromPrompt() {
        String id = readLine("Member id: ");
        String name = readLine("Name: ");
        if (id == null || name == null) {
            return;
        }
        try {
            Member member = registerMember(id, name);
            System.out.println("Registered " + member.getName() + " as " + member.getId() + ".");
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void checkOutFromPrompt() {
        String isbn = readLine("ISBN: ");
        String memberId = readLine("Member id: ");
        if (isbn == null || memberId == null) {
            return;
        }
        LocalDate taken = readDate("Checkout date [" + workingDate + "]: ");
        if (taken == null) {
            return;
        }
        try {
            Loan loan = checkOut(isbn, memberId, taken);
            LibraryPrinter.printReceipt(loan);
        } catch (BookNotFoundException | MemberNotFoundException | BookUnavailableException
                 | LoanLimitException | DuplicateLoanException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void returnFromPrompt() {
        String isbn = readLine("ISBN: ");
        String memberId = readLine("Member id: ");
        if (isbn == null || memberId == null) {
            return;
        }
        LocalDate returned = readDate("Return date [" + workingDate + "]: ");
        if (returned == null) {
            return;
        }
        try {
            Member member = member(memberId);
            Loan loan = member.openLoanFor(book(isbn).getIsbn());
            BigDecimal fine = returnBook(isbn, memberId, returned);
            LibraryPrinter.printReturn(loan, fine);
        } catch (BookNotFoundException | MemberNotFoundException | LoanNotFoundException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void overdueReport() {
        List<Loan> overdue = overdueLoans(workingDate);
        LibraryPrinter.printLoans(overdue, workingDate, "OVERDUE");
        if (!overdue.isEmpty()) {
            System.out.println("Fines running on open loans: "
                    + LibraryPrinter.money(finesOutstanding(workingDate)));
        }
    }

    /** Accepts a date, or "+7" to move the desk forward seven days. */
    private void changeWorkingDate() {
        String answer = readLine("Date as yyyy-MM-dd, or +days [" + workingDate + "]: ");
        if (answer == null || answer.isBlank()) {
            return;
        }
        String value = answer.trim();
        try {
            if (value.startsWith("+")) {
                workingDate = workingDate.plusDays(Long.parseLong(value.substring(1)));
            } else {
                workingDate = LocalDate.parse(value);
            }
            System.out.println("Working date is now " + workingDate + ".");
        } catch (NumberFormatException | DateTimeParseException ex) {
            System.out.println("Rejected: '" + value + "' is not a date or a number of days.");
        }
    }

    private void loadCatalogueFromPrompt() {
        String answer = readLine("CSV path [" + DEFAULT_CSV + "]: ");
        if (answer == null) {
            return;
        }
        loadCatalogue(Path.of(answer.isBlank() ? DEFAULT_CSV : answer.trim()));
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return input.nextLine();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /** Blank input means the working date. Returns null only when input ends. */
    private LocalDate readDate(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) {
                return null;
            }
            if (line.isBlank()) {
                return workingDate;
            }
            try {
                return LocalDate.parse(line.trim());
            } catch (DateTimeParseException ex) {
                System.out.println("'" + line.trim() + "' is not a date, use yyyy-MM-dd.");
            }
        }
    }
}
