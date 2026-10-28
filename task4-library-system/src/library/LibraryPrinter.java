package library;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** Every table and receipt the desk prints. */
public final class LibraryPrinter {

    private static final String RULE = "-".repeat(64);

    private LibraryPrinter() {
    }

    private static final String CATALOGUE_FORMAT = "%-15s %-26s %-20s %-22s %6s %10s  %s";

    public static void printCatalogue(Collection<Book> books, String heading) {
        String header = String.format(CATALOGUE_FORMAT,
                "ISBN", "TITLE", "AUTHOR", "GENRE", "YEAR", "AVAILABLE", "STATUS");
        String rule = "-".repeat(header.length());
        System.out.println(rule);
        System.out.println(heading);
        System.out.println(rule);
        if (books.isEmpty()) {
            System.out.println("Nothing to show.");
            System.out.println(rule);
            return;
        }
        System.out.println(header);
        for (Book book : books) {
            System.out.println(String.format(CATALOGUE_FORMAT,
                    book.getIsbn(), cut(book.getTitle(), 26), cut(book.getAuthor(), 20), cut(book.getGenre(), 22),
                    book.getYear() == 0 ? "-" : String.valueOf(book.getYear()),
                    book.getCopiesAvailable() + "/" + book.getTotalCopies(), book.status()));
        }
        System.out.println(rule);
        System.out.printf("%d title(s).%n", books.size());
    }

    private static final String MEMBER_FORMAT = "%-8s %-24s %-12s %6s %12s %12s";

    public static void printMembers(Collection<Member> members, LocalDate asOf) {
        String header = String.format(MEMBER_FORMAT,
                "ID", "NAME", "JOINED", "OUT", "FINES DUE", "FINES PAID");
        String rule = "-".repeat(header.length());
        System.out.println(rule);
        System.out.println("MEMBERS");
        System.out.println(rule);
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            System.out.println(rule);
            return;
        }
        System.out.println(header);
        for (Member member : members) {
            System.out.println(String.format(MEMBER_FORMAT,
                    member.getId(), cut(member.getName(), 24), member.getJoined(),
                    String.valueOf(member.activeLoans().size()),
                    money(member.finesOutstanding(asOf)), money(member.getFinesPaid())));
        }
        System.out.println(rule);
    }

    private static final String LOAN_FORMAT = "%-15s %-26s %-8s %-12s %-12s %6s %10s";

    public static void printLoans(List<Loan> loans, LocalDate asOf, String heading) {
        String header = String.format(LOAN_FORMAT,
                "ISBN", "TITLE", "MEMBER", "TAKEN", "DUE", "LATE", "FINE");
        String rule = "-".repeat(header.length());
        System.out.println(rule);
        System.out.printf("%s (as at %s)%n", heading, asOf);
        System.out.println(rule);
        if (loans.isEmpty()) {
            System.out.println("Nothing to show.");
            System.out.println(rule);
            return;
        }
        System.out.println(header);
        for (Loan loan : loans) {
            long late = loan.daysOverdue(asOf);
            System.out.println(String.format(LOAN_FORMAT,
                    loan.getBook().getIsbn(), cut(loan.getBook().getTitle(), 26), loan.getMember().getId(),
                    loan.getCheckedOut(), loan.getDueOn(),
                    late == 0 ? "-" : late + "d", late == 0 ? "-" : money(loan.fine(asOf))));
        }
        System.out.println(rule);
        System.out.printf("%d loan(s).%n", loans.size());
    }

    public static void printReceipt(Loan loan) {
        System.out.println(RULE);
        System.out.println("CHECKOUT RECEIPT");
        System.out.printf("Title   : %s by %s%n", loan.getBook().getTitle(), loan.getBook().getAuthor());
        System.out.printf("ISBN    : %s%n", loan.getBook().getIsbn());
        System.out.printf("Member  : %s (%s)%n", loan.getMember().getName(), loan.getMember().getId());
        System.out.printf("Taken   : %s%n", loan.getCheckedOut());
        System.out.printf("Due     : %s (%d day loan)%n", loan.getDueOn(), Loan.LOAN_DAYS);
        System.out.printf("Copies  : %d of %d left on the shelf%n",
                loan.getBook().getCopiesAvailable(), loan.getBook().getTotalCopies());
        System.out.println(RULE);
    }

    public static void printReturn(Loan loan, BigDecimal fine) {
        System.out.println(RULE);
        System.out.println("RETURN");
        System.out.printf("Title   : %s%n", loan.getBook().getTitle());
        System.out.printf("Member  : %s (%s)%n", loan.getMember().getName(), loan.getMember().getId());
        System.out.printf("Due     : %s%n", loan.getDueOn());
        System.out.printf("Returned: %s%n", loan.getReturnedOn());
        long late = loan.daysOverdue(loan.getReturnedOn());
        if (late == 0) {
            System.out.println("On time, no fine.");
        } else {
            System.out.printf("%d day(s) late at %s a day, fine %s%n",
                    late, money(Loan.FINE_PER_DAY), money(fine));
            if (fine.compareTo(Loan.MAX_FINE) >= 0) {
                System.out.printf("Fine capped at %s.%n", money(Loan.MAX_FINE));
            }
        }
        System.out.println(RULE);
    }

    public static String money(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }

    private static String cut(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width - 1) + ".";
    }
}
