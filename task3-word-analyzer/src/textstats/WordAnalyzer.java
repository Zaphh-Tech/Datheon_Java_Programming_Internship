package textstats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Word frequency and readability figures for a text file.
 *
 * Usage:
 *   java textstats.WordAnalyzer [file] [-n count] [--keep-stopwords]
 *   java textstats.WordAnalyzer -            reads standard input
 *
 * With no file it reads ../data/sample_article.txt, the sample that ships with
 * the task. Common English words are filtered out of the ranking by default,
 * because otherwise "the" and "and" own the top of every list; --keep-stopwords
 * turns that off and ranks the raw counts.
 */
public class WordAnalyzer {

    private static final String DEFAULT_FILE = "../data/sample_article.txt";
    private static final int DEFAULT_TOP = 10;
    private static final int BAR_WIDTH = 30;
    private static final String RULE = "-".repeat(64);

    public static void main(String[] args) {
        String file = DEFAULT_FILE;
        int top = DEFAULT_TOP;
        boolean removeStopWords = true;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h", "--help" -> {
                    printUsage();
                    return;
                }
                case "--keep-stopwords" -> removeStopWords = false;
                case "-n" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("-n needs a number, for example -n 15");
                        System.exit(1);
                    }
                    try {
                        top = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException ex) {
                        System.err.println("'" + args[i] + "' is not a number");
                        System.exit(1);
                    }
                    if (top <= 0) {
                        System.err.println("-n has to be at least 1");
                        System.exit(1);
                    }
                }
                default -> {
                    if (arg.startsWith("-") && arg.length() > 1 && !arg.equals("-")) {
                        System.err.println("Unknown option: " + arg);
                        printUsage();
                        System.exit(1);
                    }
                    file = arg;
                }
            }
        }

        try {
            List<String> lines = readLines(file);
            String text = String.join("\n", lines);
            TextStatistics stats = new TextStatistics(text, removeStopWords);
            printSummary(file, lines.size(), stats);
            printRanking(stats, top);
        } catch (NoSuchFileException ex) {
            System.err.println("No such file: " + ex.getMessage());
            System.err.println("Pass a path, or run it from the task folder so " + DEFAULT_FILE + " resolves.");
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Could not read the text: " + ex.getMessage());
            System.exit(1);
        }
    }

    /** Reads the whole document a line at a time; "-" means standard input. */
    private static List<String> readLines(String file) throws IOException {
        if (file.equals("-")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                return collect(reader);
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            return collect(reader);
        }
    }

    private static List<String> collect(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static void printSummary(String file, int lineCount, TextStatistics stats) {
        System.out.println(RULE);
        System.out.println("TEXT SUMMARY");
        System.out.println(RULE);
        System.out.printf("%-24s %s%n", "Source", file.equals("-") ? "standard input" : file);
        System.out.printf("%-24s %d%n", "Lines", lineCount);
        System.out.printf("%-24s %d%n", "Characters", stats.getCharacterCount());
        System.out.printf("%-24s %d%n", "Letters", stats.getLetterCount());
        System.out.printf("%-24s %d%n", "Sentences", stats.getSentenceCount());
        System.out.printf("%-24s %d%n", "Total words", stats.getTotalWords());
        System.out.printf("%-24s %d%n", "Unique words", stats.getUniqueWords());
        System.out.printf("%-24s %d%n", "Words used once", stats.hapaxCount());
        System.out.printf("%-24s %.2f words%n", "Average sentence", stats.averageSentenceLength());
        System.out.printf("%-24s %.2f characters%n", "Average word", stats.averageWordLength());
        System.out.printf("%-24s %.2f%n", "Lexical diversity", stats.lexicalDiversity());
        System.out.printf("%-24s %s (%d characters)%n", "Longest word",
                stats.getLongestWord(), stats.getLongestWord().length());
        System.out.println(RULE);
    }

    private static void printRanking(TextStatistics stats, int top) {
        List<Map.Entry<String, Integer>> ranked = stats.topWords(top);
        if (ranked.isEmpty()) {
            System.out.println("No words to rank.");
            return;
        }
        int highest = ranked.get(0).getValue();
        int rankedTotal = stats.getRankedWordCount();

        System.out.printf("TOP %d WORDS%s%n", ranked.size(),
                stats.isStopWordsRemoved() ? " (common English words filtered out)" : " (nothing filtered)");
        System.out.println(RULE);
        System.out.printf("%4s  %-20s %6s %7s%n", "RANK", "WORD", "COUNT", "SHARE");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranked) {
            double share = rankedTotal == 0 ? 0.0 : 100.0 * entry.getValue() / rankedTotal;
            int bar = Math.max(1, BAR_WIDTH * entry.getValue() / highest);
            System.out.printf("%4d  %-20s %6d %6.2f%%  %s%n", rank++, entry.getKey(), entry.getValue(),
                    share, "#".repeat(bar));
        }
        System.out.println(RULE);
        if (stats.isStopWordsRemoved()) {
            System.out.printf("%d stop words are excluded from the ranking. "
                    + "Run with --keep-stopwords to include them.%n", StopWords.size());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java textstats.WordAnalyzer [file] [-n count] [--keep-stopwords]");
        System.out.println("  file               text file to analyse, or - for standard input");
        System.out.println("                     (default " + DEFAULT_FILE + ")");
        System.out.println("  -n count           how many words to rank (default " + DEFAULT_TOP + ")");
        System.out.println("  --keep-stopwords   rank common English words as well");
    }
}
