# Datheon Java Programming Internship

Four console applications written for the Datheon Java track: a student grade
manager, an ATM and account engine, a text analyzer and a library system. All
four tasks in the pack are here; the brief asked for two or three.

Each task is a self contained folder with its own sources, its own package and
its own README explaining the design, the policy constants and the sample
output. The datasets that came with the tasks are in `data/`.

| Task | Folder | Entry point |
| --- | --- | --- |
| 1. Student grade management | [`task1-grade-manager`](task1-grade-manager) | `grades.GradeManager` |
| 2. ATM and account engine | [`task2-atm-banking`](task2-atm-banking) | `banking.ATM` |
| 3. Word analyzer | [`task3-word-analyzer`](task3-word-analyzer) | `textstats.WordAnalyzer` |
| 4. Library management | [`task4-library-system`](task4-library-system) | `library.LibrarySystem` |

## Building and running

Written and tested on OpenJDK 25, no build tool and no third party libraries.
Anything from JDK 17 onwards will compile it: the newest language features used
are records, switch expressions and pattern matching for `instanceof`.

    ./build.sh                    # compiles all four tasks into their out/ folders

Or one at a time, from inside the task folder:

    cd task1-grade-manager
    javac -d out src/grades/*.java
    java -cp out grades.GradeManager ../data/sample_student_grades.csv

    cd task2-atm-banking
    javac -d out src/banking/*.java
    java -cp out banking.ATM

    cd task3-word-analyzer
    javac -d out src/textstats/*.java
    java -cp out textstats.WordAnalyzer ../data/sample_article.txt

    cd task4-library-system
    javac -d out src/library/*.java
    java -cp out library.LibrarySystem ../data/books_catalog.csv

The run commands assume you are inside the task folder, because the default
data paths are written relative to it. Every program also takes the file as an
argument, so they work from anywhere if you pass an absolute path.

## Layout

    data/                        the datasets supplied with the tasks
    task1-grade-manager/
        src/grades/              Student, Grading, ClassStatistics, ReportPrinter,
                                 StudentCsvReader, GradeManager
    task2-atm-banking/
        src/banking/             Account, SavingsAccount, CheckingAccount, Customer,
                                 Pin, Bank, Transaction, TransactionLog, Money,
                                 StatementPrinter, ATM, five exceptions
    task3-word-analyzer/
        src/textstats/           TextStatistics, StopWords, WordAnalyzer
    task4-library-system/
        src/library/             Book, Member, Loan, LibrarySystem, BookCsvReader,
                                 LibraryPrinter, six exceptions

## Conventions used across all four tasks

Validation happens at the boundary of the object that owns the data. A mark
outside 0 to 100 is refused by `Student`, a negative deposit by `Money`, a
return before the checkout date by `LibrarySystem`. Nothing downstream has to
re-check, and no caller can bypass it.

State changes go through one method that also records them. An account balance
only moves through `Account.post`, which writes the audit line in the same
step, and a copy only leaves the shelf through `Book.lendCopy`. Those mutators
are protected or package private, so the invariant cannot be worked around
from outside.

Printing is separate from logic. `ReportPrinter`, `StatementPrinter` and
`LibraryPrinter` hold every table; the model classes return numbers and never
print. That is what makes the logic classes usable without the menu.

Money is `BigDecimal` at two decimal places. A `double` cannot represent 0.10
exactly, which matters as soon as interest is posted twelve times.

The CSV readers find columns by name from the header rather than by position,
and report a bad row with its line number instead of skipping it quietly.

Menus end cleanly when input runs out rather than throwing
`NoSuchElementException`, so a session can be driven from a pipe or a file as
well as by hand. That is how the sample output in each README was produced.

## Data

| File | Used by |
| --- | --- |
| `data/sample_student_grades.csv` | Task 1 |
| `data/sample_article.txt` | Task 3 |
| `data/books_catalog.csv` | Task 4 |

Task 2 has no dataset in the pack, so it opens four demo accounts in code. Their
numbers and PINs are in that task's README.
