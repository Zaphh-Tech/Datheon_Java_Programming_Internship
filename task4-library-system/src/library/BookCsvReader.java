package library;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the catalogue from the CSV export.
 *
 * Expected header:
 *   isbn,title,author,genre,year,copies_available
 *
 * Columns are found by name rather than by position, so a file with the same
 * columns in a different order still loads.
 */
public final class BookCsvReader {

    private BookCsvReader() {
    }

    public static List<Book> read(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException(file + " is empty");
            }
            String[] header = splitCsvLine(headerLine);
            int isbn = require(file, header, "isbn");
            int title = require(file, header, "title");
            int author = indexOf(header, "author");
            int genre = indexOf(header, "genre");
            int year = indexOf(header, "year");
            int copies = indexOf(header, "copies_available");

            List<Book> books = new ArrayList<>();
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
                try {
                    books.add(new Book(
                            fields[isbn],
                            fields[title],
                            author < 0 ? null : fields[author],
                            genre < 0 ? null : fields[genre],
                            year < 0 ? 0 : parseInt(fields[year], 0),
                            copies < 0 ? 1 : parseInt(fields[copies], 1)));
                } catch (IllegalArgumentException ex) {
                    throw new IOException(file + " line " + lineNumber + ": " + ex.getMessage());
                }
            }
            return books;
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("'" + value.trim() + "' is not a whole number");
        }
    }

    private static int require(Path file, String[] header, String column) throws IOException {
        int index = indexOf(header, column);
        if (index < 0) {
            throw new IOException(file + ": the header has no " + column + " column");
        }
        return index;
    }

    private static int indexOf(String[] header, String column) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equalsIgnoreCase(column)) {
                return i;
            }
        }
        return -1;
    }

    /** Comma split that respects double quotes, so "Martin, Robert C." stays one field. */
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
