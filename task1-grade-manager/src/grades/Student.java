package grades;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single student record: identifier, name and the marks recorded per subject.
 * Marks live in a LinkedHashMap so report cards list subjects in the order the
 * teacher entered them instead of a random hash order.
 */
public class Student {

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;

    private final int id;
    private String name;
    private final Map<String, Integer> scores = new LinkedHashMap<>();

    public Student(int id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("student id must be positive, got " + id);
        }
        this.id = id;
        this.name = requireName(name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireName(name);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("student name cannot be empty");
        }
        return name.trim();
    }

    /**
     * Records a mark, replacing any earlier mark for the same subject.
     * Anything outside 0..100 is rejected here rather than later during the
     * average, so a bad keystroke never reaches the report card.
     */
    public void setScore(String subject, int score) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject cannot be empty");
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("score for " + subject.trim() + " must be between "
                    + MIN_SCORE + " and " + MAX_SCORE + ", got " + score);
        }
        scores.put(matchingKey(subject.trim()), score);
    }

    public boolean hasScore(String subject) {
        return scores.containsKey(matchingKey(subject.trim()));
    }

    public int getScore(String subject) {
        Integer score = scores.get(matchingKey(subject.trim()));
        if (score == null) {
            throw new IllegalArgumentException("no mark recorded for " + subject + " (student " + id + ")");
        }
        return score;
    }

    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public int subjectCount() {
        return scores.size();
    }

    /** Mean of every recorded mark, or 0 for a student with no marks yet. */
    public double average() {
        if (scores.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (int score : scores.values()) {
            total += score;
        }
        return (double) total / scores.size();
    }

    /** Cumulative GPA on a 4.0 scale: the mean of the per-subject grade points. */
    public double gpa() {
        if (scores.isEmpty()) {
            return 0.0;
        }
        double points = 0.0;
        for (int score : scores.values()) {
            points += Grading.gradePoint(score);
        }
        return points / scores.size();
    }

    public String letterGrade() {
        return Grading.letter((int) Math.round(average()));
    }

    /**
     * Keeps "Math" and "math" from becoming two separate subjects: if a subject
     * is already on file under a different case, reuse that key.
     */
    private String matchingKey(String subject) {
        for (String existing : scores.keySet()) {
            if (existing.equalsIgnoreCase(subject)) {
                return existing;
            }
        }
        return subject;
    }

    @Override
    public String toString() {
        return id + " " + name + " (" + scores.size() + " subjects, avg " + String.format("%.2f", average()) + ")";
    }
}
