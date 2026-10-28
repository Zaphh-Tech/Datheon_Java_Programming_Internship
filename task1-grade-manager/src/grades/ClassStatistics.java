package grades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cohort level numbers derived from the student records. Everything here works
 * off a snapshot of the list, so the caller can print statistics without the
 * statistics code being able to modify the roster.
 */
public final class ClassStatistics {

    private final List<Student> students;

    public ClassStatistics(List<Student> students) {
        this.students = new ArrayList<>(students);
    }

    public boolean isEmpty() {
        return graded().isEmpty();
    }

    public int gradedCount() {
        return graded().size();
    }

    public double classAverage() {
        List<Student> graded = graded();
        if (graded.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Student student : graded) {
            total += student.average();
        }
        return total / graded.size();
    }

    /**
     * Median of the student averages. With an even number of students it is the
     * mean of the two middle values, which is why this is not just the middle
     * element of the sorted list.
     */
    public double medianAverage() {
        List<Double> averages = new ArrayList<>();
        for (Student student : graded()) {
            averages.add(student.average());
        }
        if (averages.isEmpty()) {
            return 0.0;
        }
        Collections.sort(averages);
        int middle = averages.size() / 2;
        if (averages.size() % 2 == 1) {
            return averages.get(middle);
        }
        return (averages.get(middle - 1) + averages.get(middle)) / 2.0;
    }

    public double classGpa() {
        List<Student> graded = graded();
        if (graded.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Student student : graded) {
            total += student.gpa();
        }
        return total / graded.size();
    }

    public Student topStudent() {
        Student best = null;
        for (Student student : graded()) {
            if (best == null || student.average() > best.average()) {
                best = student;
            }
        }
        return best;
    }

    public Student lowestStudent() {
        Student worst = null;
        for (Student student : graded()) {
            if (worst == null || student.average() < worst.average()) {
                worst = student;
            }
        }
        return worst;
    }

    /** Average mark per subject, keyed by subject in alphabetical order. */
    public Map<String, Double> subjectAverages() {
        Map<String, Integer> totals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Student student : graded()) {
            for (Map.Entry<String, Integer> entry : student.getScores().entrySet()) {
                totals.merge(entry.getKey(), entry.getValue(), Integer::sum);
                counts.merge(entry.getKey(), 1, Integer::sum);
            }
        }
        Map<String, Double> averages = new LinkedHashMap<>();
        for (String subject : totals.keySet()) {
            averages.put(subject, (double) totals.get(subject) / counts.get(subject));
        }
        return averages;
    }

    /** How many students fall into each letter band, A through F. */
    public Map<String, Integer> gradeDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (String letter : new String[] {"A", "B", "C", "D", "F"}) {
            distribution.put(letter, 0);
        }
        for (Student student : graded()) {
            distribution.merge(student.letterGrade(), 1, Integer::sum);
        }
        return distribution;
    }

    /** Students above the class average, best first. */
    public List<Student> aboveAverage() {
        double average = classAverage();
        List<Student> above = new ArrayList<>();
        for (Student student : graded()) {
            if (student.average() > average) {
                above.add(student);
            }
        }
        above.sort((left, right) -> Double.compare(right.average(), left.average()));
        return above;
    }

    private List<Student> graded() {
        List<Student> graded = new ArrayList<>();
        for (Student student : students) {
            if (student.subjectCount() > 0) {
                graded.add(student);
            }
        }
        return graded;
    }
}
