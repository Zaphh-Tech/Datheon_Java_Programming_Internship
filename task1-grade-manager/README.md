# Task 1: Student Grade Management System

A console application for a teacher or an administrator: register students,
record a mark per subject, and print report cards and class statistics. Marks
are validated on the way in, so a typo cannot reach the report card.

## Running it

    cd task1-grade-manager
    javac -d out src/grades/*.java
    java -cp out grades.GradeManager ../data/sample_student_grades.csv

The CSV path is optional. Without it the program starts empty and menu option 7
loads a file at any time. Option 7 defaults to `../data/sample_student_grades.csv`,
which is the sample that came with the task.

## Menu

    1) Add student
    2) Record marks
    3) Show roster
    4) Class average and median
    5) Report card for one student
    6) Report cards for the whole class
    7) Load students from CSV
    8) Remove student
    0) Exit

## Classes

| File | Responsibility |
| --- | --- |
| `Student.java` | One student: id, name and the marks map. Rejects a mark outside 0 to 100 and an empty name. |
| `Grading.java` | The grading scale in one place: score to grade point, score to letter, average to remark. |
| `ClassStatistics.java` | Cohort figures: average, median, class GPA, per subject averages, grade distribution. |
| `ReportPrinter.java` | Every table, laid out with `String.format`. |
| `StudentCsvReader.java` | Reads the cohort from CSV. Columns are found by name, not by position. |
| `GradeManager.java` | Holds the `ArrayList<Student>` and runs the menu. |

## Grading scale

| Score | Grade point | Letter |
| --- | --- | --- |
| 90 and above | 4.0 | A |
| 80 to 89 | 3.0 | B |
| 70 to 79 | 2.0 | C |
| 60 to 69 | 1.0 | D |
| below 60 | 0.0 | F |

A student's GPA is the mean of their per subject grade points, so a four
subject student scoring 88, 92, 79 and 95 gets 3.0 + 4.0 + 2.0 + 4.0 over four,
which is 3.25. The class GPA is the mean of the student GPAs.

## Decisions worth noting

Marks live in a `LinkedHashMap`, so report cards list subjects in the order the
teacher entered them instead of a hash order that changes between runs.

The subject key is matched case insensitively. Typing `math` after `Math` is
already on file updates the existing subject instead of creating a second one.

The CSV reader treats any column named `<something>_score` as a subject, so a
file with an extra subject column loads without a code change. Bad rows are
reported with their line number rather than skipped silently.

Students with no marks yet are kept out of the averages. Counting them as zero
would drag the class average down for a data entry gap.

## Sample output

Roster after loading the sample file and adding one student by hand:

    ID     NAME                     Math  Science  English Computer  AVERAGE    GPA GRADE
    ------------------------------------------------------------------------------------
    101    Aarav Sharma               88       92       79       95    88.50   3.25     B
    102    Diya Patel                 95       89       94       98    94.00   3.75     A
    103    Rohan Gupta                72       68       75       80    73.75   2.00     C
    104    Ananya Iyer                91       96       88       94    92.25   3.75     A
    105    Vikram Singh               65       70       72       68    68.75   1.50     D

Class summary, menu option 4:

    Students with marks : 5
    Class average       : 83.45
    Median average      : 88.50
    Class GPA           : 2.85 / 4.00
    Highest             : Diya Patel (94.00)
    Lowest              : Vikram Singh (68.75)

    SUBJECT               AVERAGE   GRADE
    Computer                87.00       B
    English                 81.60       B
    Math                    82.20       B
    Science                 83.00       B

A mark outside the range is refused at the point of entry:

    Subject: Math
    Score for Math (0-100): 120
    Rejected: score for Math must be between 0 and 100, got 120
