package grades;

/**
 * The grading scale, kept in one place so the report card, the GPA and the
 * class statistics can never drift apart.
 */
public final class Grading {

    private Grading() {
    }

    public static double gradePoint(int score) {
        if (score >= 90) {
            return 4.0;
        }
        if (score >= 80) {
            return 3.0;
        }
        if (score >= 70) {
            return 2.0;
        }
        if (score >= 60) {
            return 1.0;
        }
        return 0.0;
    }

    public static String letter(int score) {
        if (score >= 90) {
            return "A";
        }
        if (score >= 80) {
            return "B";
        }
        if (score >= 70) {
            return "C";
        }
        if (score >= 60) {
            return "D";
        }
        return "F";
    }

    public static String remark(double average) {
        if (average >= 90) {
            return "Excellent";
        }
        if (average >= 80) {
            return "Very good";
        }
        if (average >= 70) {
            return "Good";
        }
        if (average >= 60) {
            return "Needs improvement";
        }
        return "At risk";
    }
}
