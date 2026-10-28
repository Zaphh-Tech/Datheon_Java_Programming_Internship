# Task 4: Library Management System

A library desk application: catalogue, members, checkouts, returns, due dates
and overdue fines.

## Running it

    cd task4-library-system
    javac -d out src/library/*.java
    java -cp out library.LibrarySystem ../data/books_catalog.csv

The CSV path is optional and defaults to `../data/books_catalog.csv`. Two
members, M-01 and M-02, are registered at startup so there is somebody to lend
to; option 3 registers more.

## Menu

    1) Show catalogue
    2) Search the catalogue
    3) Register a member
    4) Show members
    5) Check out a book
    6) Return a book
    7) Open loans
    8) Overdue report
    9) Set the working date
    10) Load catalogue from CSV
    0) Exit

## Classes

| File | Responsibility |
| --- | --- |
| `Book.java` | A catalogued title and its copy counts. Lends and shelves one copy at a time. |
| `Member.java` | A member and their whole borrowing history, open loans included. |
| `Loan.java` | One copy with one member: dates out and due, the return date, and the fine derived from them. |
| `LibrarySystem.java` | The catalogue map, the member map, the loan list, the search filters and the menu. |
| `BookCsvReader.java` | Reads the catalogue from CSV, finding columns by name. |
| `LibraryPrinter.java` | Catalogue, member, loan and overdue tables, and the checkout and return receipts. |

## Custom exceptions

| Exception | Thrown when |
| --- | --- |
| `BookNotFoundException` | The ISBN is not catalogued. |
| `MemberNotFoundException` | The member id is not registered. |
| `BookUnavailableException` | Every copy of the title is already on loan. |
| `LoanLimitException` | The member already holds three books. |
| `DuplicateLoanException` | The member already has that title out. |
| `LoanNotFoundException` | A return is attempted for a book the member does not have out. |

## Policy

| Rule | Value |
| --- | --- |
| Loan period | 14 days |
| Books per member | 3 |
| Late fee | 5.00 a day |
| Fine cap | 250.00 per loan |

## Decisions worth noting

Availability is a copy count, not an on-loan flag. The sample catalogue has
five copies of Clean Code, which can be lent five times, and a boolean cannot
express that. `status()` derives "On shelf", "Partly out" or "All on loan" from
the two counters so there is no third piece of state to keep in step.

The fine is computed from the dates rather than stored, so it cannot fall out
of step with them. `daysOverdue` counts against the return date once the book
is back and against the date you ask about while it is still out, which is why
every caller has to pass the date it means.

Dates come from a working date held by `LibrarySystem`, not from
`LocalDate.now()`. The desk can back-date an entry, and option 9 moves the date
forward with `+20`, which is how overdue behaviour gets demonstrated without
waiting two weeks.

`lendCopy` and `shelveCopy` are package private, so only the library lends
books. Checkout takes the copy off the shelf first: if it throws, no loan
record was created, so the shelf count and the loan list cannot disagree.
`shelveCopy` throws if more copies come back than went out, because quietly
accepting that would hide a bug in the ledger.

Returned loans stay on the member's list. The history is what tells the desk
whether somebody keeps bringing books back late.

## Sample output

Catalogue after lending three titles:

    ISBN            TITLE                      AUTHOR               GENRE                    YEAR  AVAILABLE  STATUS
    978-0134685991  Effective Java             Joshua Bloch         Technology               2018        3/4  Partly out
    978-0596009205  Head First Java            Kathy Sierra         Technology               2005        1/2  Partly out
    978-0132350884  Clean Code                 Robert C. Martin     Software Engineering     2008        4/5  Partly out
    978-0201633610  Design Patterns            Erich Gamma          Computer Science         1994        3/3  On shelf

Checkout receipt:

    CHECKOUT RECEIPT
    Title   : Clean Code by Robert C. Martin
    ISBN    : 978-0132350884
    Member  : Aarav Sharma (M-01)
    Taken   : 2026-10-28
    Due     : 2026-11-11 (14 day loan)
    Copies  : 4 of 5 left on the shelf

Overdue report after moving the working date on by twenty days:

    OVERDUE (as at 2026-11-17)
    ISBN            TITLE                      MEMBER   TAKEN        DUE            LATE       FINE
    978-0132350884  Clean Code                 M-01     2026-10-28   2026-11-11       6d      30.00
    978-0596009205  Head First Java            M-01     2026-10-28   2026-11-11       6d      30.00
    978-0134685991  Effective Java             M-01     2026-10-28   2026-11-11       6d      30.00
    978-0596009205  Head First Java            M-02     2026-10-28   2026-11-11       6d      30.00
    4 loan(s).
    Fines running on open loans: 120.00

A late return:

    RETURN
    Title   : Clean Code
    Member  : Aarav Sharma (M-01)
    Due     : 2026-11-11
    Returned: 2026-11-17
    6 day(s) late at 5.00 a day, fine 30.00

The refusals:

    Rejected: Aarav Sharma already has "Clean Code" out
    Rejected: Aarav Sharma already has 3 book(s) out, the limit is 3
    Rejected: every copy of "Head First Java" is on loan
    Rejected: no book catalogued under ISBN 999-9999999999
    Rejected: member M-03 has no open loan for ISBN 978-0134685991
