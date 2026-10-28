package grades;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a cohort from a CSV export. The header drives everything: any column
 * named "<subject>_score" becomes a subject, so a file with extra subjects
 * loads without a code change.
 *
 * Expected shape:
 *   student_id,name,math_score,science_score,english_score,computer_score
 */
public final class StudentCsvReader {

    private static final String ID_COLUMN = "student_id";
    private static final String NAME_COLUMN = "name";
    private static final String SCORE_SUFFIX = "_score";

    private StudentCsvReader() {
    }

    public static List<Student> read(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException(file + " is empty");
            }
            String[] header = splitCsvLine(headerLine);
            int idIndex = indexOf(header, ID_COLUMN);
            int nameIndex = indexOf(header, NAME_COLUMN);
            if (idIndex < 0 || nameIndex < 0) {
                throw new IOException(file + ": header must contain " + ID_COLUMN + " and " + NAME_COLUMN);
            }

            List<Student> students = new ArrayList<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = splitCsvLine(line);
                if (fields.length != header.length) {
                    throw new IOException(file + " line " + lineNumber + ": expected " + header.length
                            + " fields, found " + fields.length);
                }
                students.add(toStudent(file, lineNumber, header, fields, idIndex, nameIndex));
            }
            return students;
        }
    }

    private static Student toStudent(Path file, int lineNumber, String[] header, String[] fields,
                                     int idIndex, int nameIndex) throws IOException {
        int id;
        try {
            id = Integer.parseInt(fields[idIndex]);
        } catch (NumberFormatException ex) {
            throw new IOException(file + " line " + lineNumber + ": '" + fields[idIndex] + "' is not a student id");
        }

        Student student;
        try {
            student = new Student(id, fields[nameIndex]);
        } catch (IllegalArgumentException ex) {
            throw new IOException(file + " line " + lineNumber + ": " + ex.getMessage());
        }

        for (int column = 0; column < header.length; column++) {
            if (column == idIndex || column == nameIndex) {
                continue;
            }
            String columnName = header[column].toLowerCase();
            if (!columnName.endsWith(SCORE_SUFFIX)) {
                continue;
            }
            String value = fields[column];
            if (value.isBlank()) {
                continue;
            }
            try {
                student.setScore(subjectName(columnName), Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                throw new IOException(file + " line " + lineNumber + ": '" + value + "' is not a mark");
            } catch (IllegalArgumentException ex) {
                throw new IOException(file + " line " + lineNumber + ": " + ex.getMessage());
            }
        }
        return student;
    }

    /** "computer_score" becomes "Computer", "social_studies_score" becomes "Social studies". */
    private static String subjectName(String columnName) {
        String base = columnName.substring(0, columnName.length() - SCORE_SUFFIX.length()).replace('_', ' ');
        if (base.isEmpty()) {
            return columnName;
        }
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }

    private static int indexOf(String[] header, String column) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equalsIgnoreCase(column)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Comma split that respects double quotes, so a name written as
     * "Iyer, Ananya" survives as one field.
     */
    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (current == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }
}
