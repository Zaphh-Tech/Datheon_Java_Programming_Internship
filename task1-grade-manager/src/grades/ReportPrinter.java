package grades;

import java.util.List;
import java.util.Map;

/**
 * Every table the application prints. Column widths are fixed with
 * String.format so the output stays aligned no matter how long a name is.
 */
public final class ReportPrinter {

    private static final String LINE = "-".repeat(72);

    private ReportPrinter() {
    }

    public static void printRoster(List<Student> students, List<String> subjects) {
        if (students.isEmpty()) {
            System.out.println("No students on file yet.");
            return;
        }
        StringBuilder header = new StringBuilder(String.format("%-6s %-20s", "ID", "NAME"));
        for (String subject : subjects) {
            header.append(String.format(" %8s", abbreviate(subject, 8)));
        }
        header.append(String.format(" %8s %6s %5s", "AVERAGE", "GPA", "GRADE"));
        String rule = "-".repeat(header.length());

        System.out.println(rule);
        System.out.println(header);
        System.out.println(rule);
        for (Student student : students) {
            StringBuilder row = new StringBuilder(String.format("%-6d %-20s", student.getId(),
                    abbreviate(student.getName(), 20)));
            for (String subject : subjects) {
                row.append(String.format(" %8s", student.hasScore(subject)
                        ? String.valueOf(student.getScore(subject))
                        : "-"));
            }
            row.append(String.format(" %8.2f %6.2f %5s", student.average(), student.gpa(), student.letterGrade()));
            System.out.println(row);
        }
        System.out.println(rule);
        System.out.printf("%d student(s) on file.%n", students.size());
    }

    public static void printReportCard(Student student) {
        System.out.println(LINE);
        System.out.printf("REPORT CARD%n");
        System.out.printf("Student ID : %d%n", student.getId());
        System.out.printf("Name       : %s%n", student.getName());
        System.out.println(LINE);
        if (student.subjectCount() == 0) {
            System.out.println("No marks recorded for this student.");
            System.out.println(LINE);
            return;
        }
        System.out.printf("%-20s %8s %8s %7s%n", "SUBJECT", "SCORE", "POINTS", "GRADE");
        for (Map.Entry<String, Integer> entry : student.getScores().entrySet()) {
            int score = entry.getValue();
            System.out.printf("%-20s %8d %8.1f %7s%n", abbreviate(entry.getKey(), 20), score,
                    Grading.gradePoint(score), Grading.letter(score));
        }
        System.out.println(LINE);
        System.out.printf("Subjects   : %d%n", student.subjectCount());
        System.out.printf("Average    : %.2f%n", student.average());
        System.out.printf("GPA        : %.2f / 4.00%n", student.gpa());
        System.out.printf("Grade      : %s (%s)%n", student.letterGrade(), Grading.remark(student.average()));
        System.out.println(LINE);
    }

    public static void printStatistics(ClassStatistics stats) {
        if (stats.isEmpty()) {
            System.out.println("No marks recorded yet, so there is nothing to summarise.");
            return;
        }
        System.out.println(LINE);
        System.out.println("CLASS SUMMARY");
        System.out.println(LINE);
        System.out.printf("Students with marks : %d%n", stats.gradedCount());
        System.out.printf("Class average       : %.2f%n", stats.classAverage());
        System.out.printf("Median average      : %.2f%n", stats.medianAverage());
        System.out.printf("Class GPA           : %.2f / 4.00%n", stats.classGpa());

        Student top = stats.topStudent();
        Student lowest = stats.lowestStudent();
        System.out.printf("Highest             : %s (%.2f)%n", top.getName(), top.average());
        System.out.printf("Lowest              : %s (%.2f)%n", lowest.getName(), lowest.average());

        System.out.println();
        System.out.printf("%-20s %8s %7s%n", "SUBJECT", "AVERAGE", "GRADE");
        for (Map.Entry<String, Double> entry : stats.subjectAverages().entrySet()) {
            System.out.printf("%-20s %8.2f %7s%n", abbreviate(entry.getKey(), 20), entry.getValue(),
                    Grading.letter((int) Math.round(entry.getValue())));
        }

        System.out.println();
        System.out.println("Grade distribution");
        for (Map.Entry<String, Integer> entry : stats.gradeDistribution().entrySet()) {
            System.out.printf("  %s %-4d %s%n", entry.getKey(), entry.getValue(), "*".repeat(entry.getValue()));
        }

        List<Student> above = stats.aboveAverage();
        if (!above.isEmpty()) {
            System.out.println();
            System.out.println("Above the class average");
            for (Student student : above) {
                System.out.printf("  %-20s %.2f%n", abbreviate(student.getName(), 20), student.average());
            }
        }
        System.out.println(LINE);
    }

    private static String abbreviate(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width - 1) + ".";
    }
}
