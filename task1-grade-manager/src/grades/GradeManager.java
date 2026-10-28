package grades;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Student grade management, console front end.
 *
 * The cohort is held in an ArrayList of Student records. This class owns the
 * list and the menu; the arithmetic lives in ClassStatistics, the tables in
 * ReportPrinter and the grading scale in Grading.
 *
 * Usage:
 *   java grades.GradeManager [students.csv]
 */
public class GradeManager {

    private static final String DEFAULT_CSV = "../data/sample_student_grades.csv";

    private final List<Student> students = new ArrayList<>();
    private final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        GradeManager manager = new GradeManager();
        if (args.length > 0) {
            manager.loadCsv(Path.of(args[0]));
        }
        manager.run();
    }

    private void run() {
        System.out.println("Student Grade Management System");
        System.out.println("Type the number of the option you want.");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Choice: ");
            if (choice == null) {
                System.out.println();
                System.out.println("Input ended, closing.");
                return;
            }
            switch (choice.trim()) {
                case "1" -> addStudent();
                case "2" -> recordMarks();
                case "3" -> ReportPrinter.printRoster(students, subjects());
                case "4" -> ReportPrinter.printStatistics(new ClassStatistics(students));
                case "5" -> singleReportCard();
                case "6" -> allReportCards();
                case "7" -> loadCsvFromPrompt();
                case "8" -> removeStudent();
                case "0" -> running = false;
                case "" -> { }
                default -> System.out.println("Unknown option: " + choice);
            }
        }
        System.out.println("Closing. " + students.size() + " student(s) were on file.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1) Add student");
        System.out.println("2) Record marks");
        System.out.println("3) Show roster");
        System.out.println("4) Class average and median");
        System.out.println("5) Report card for one student");
        System.out.println("6) Report cards for the whole class");
        System.out.println("7) Load students from CSV");
        System.out.println("8) Remove student");
        System.out.println("0) Exit");
    }

    private void addStudent() {
        Integer id = readInt("Student id: ");
        if (id == null) {
            return;
        }
        if (findById(id) != null) {
            System.out.println("Student " + id + " already exists.");
            return;
        }
        String name = readLine("Full name: ");
        if (name == null) {
            return;
        }
        try {
            students.add(new Student(id, name));
            System.out.println("Added " + name.trim() + " with id " + id + ".");
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void recordMarks() {
        Student student = promptForStudent();
        if (student == null) {
            return;
        }
        System.out.println("Recording marks for " + student.getName() + ". Blank subject stops.");
        while (true) {
            String subject = readLine("Subject: ");
            if (subject == null || subject.isBlank()) {
                return;
            }
            Integer score = readInt("Score for " + subject.trim() + " (0-100): ");
            if (score == null) {
                return;
            }
            try {
                student.setScore(subject, score);
                System.out.printf("Recorded %s = %d (%s).%n", subject.trim(), score, Grading.letter(score));
            } catch (IllegalArgumentException ex) {
                System.out.println("Rejected: " + ex.getMessage());
            }
        }
    }

    private void singleReportCard() {
        Student student = promptForStudent();
        if (student != null) {
            ReportPrinter.printReportCard(student);
        }
    }

    private void allReportCards() {
        if (students.isEmpty()) {
            System.out.println("No students on file yet.");
            return;
        }
        for (Student student : students) {
            ReportPrinter.printReportCard(student);
        }
    }

    private void removeStudent() {
        Student student = promptForStudent();
        if (student == null) {
            return;
        }
        students.remove(student);
        System.out.println("Removed " + student.getName() + ".");
    }

    private void loadCsvFromPrompt() {
        String answer = readLine("CSV path [" + DEFAULT_CSV + "]: ");
        if (answer == null) {
            return;
        }
        String path = answer.isBlank() ? DEFAULT_CSV : answer.trim();
        loadCsv(Path.of(path));
    }

    /** Adds every student in the file, skipping ids that are already on file. */
    private void loadCsv(Path file) {
        try {
            List<Student> loaded = StudentCsvReader.read(file);
            int added = 0;
            int skipped = 0;
            for (Student student : loaded) {
                if (findById(student.getId()) == null) {
                    students.add(student);
                    added++;
                } else {
                    skipped++;
                }
            }
            System.out.printf("Loaded %d student(s) from %s", added, file);
            if (skipped > 0) {
                System.out.printf(", skipped %d duplicate id(s)", skipped);
            }
            System.out.println(".");
        } catch (IOException ex) {
            System.out.println("Could not read the file: " + ex.getMessage());
        }
    }

    private Student promptForStudent() {
        Integer id = readInt("Student id: ");
        if (id == null) {
            return null;
        }
        Student student = findById(id);
        if (student == null) {
            System.out.println("No student with id " + id + ".");
        }
        return student;
    }

    private Student findById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    /** Union of the subjects on file, in the order they first appeared. */
    private List<String> subjects() {
        Set<String> ordered = new LinkedHashSet<>();
        for (Student student : students) {
            ordered.addAll(student.getScores().keySet());
        }
        return new ArrayList<>(ordered);
    }

    /** Returns null when the input stream is exhausted, which ends the session. */
    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return input.nextLine();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private Integer readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) {
                return null;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("'" + line.trim() + "' is not a whole number, try again.");
            }
        }
    }
}
