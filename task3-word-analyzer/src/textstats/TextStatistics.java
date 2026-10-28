package textstats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The analysis of one body of text: tokenising, counting and ranking.
 *
 * Cleaning happens once, in the constructor. Everything after that reads from
 * the frequency map, so asking for the top ten twice costs nothing and cannot
 * disagree with the totals.
 */
public class TextStatistics {

    /** Anything that is not a letter, digit or underscore separates two words. */
    private static final String WORD_SEPARATOR = "\\W+";

    /** A full stop, question mark or exclamation mark ends a sentence. */
    private static final String SENTENCE_SEPARATOR = "[.!?]+";

    private final boolean stopWordsRemoved;
    private final Map<String, Integer> frequencies = new HashMap<>();
    private final int totalWords;
    private final int sentenceCount;
    private final int characterCount;
    private final int letterCount;
    private long totalWordLength;
    private String longestWord = "";

    public TextStatistics(String text, boolean removeStopWords) {
        this.stopWordsRemoved = removeStopWords;
        String source = text == null ? "" : text;
        this.characterCount = source.length();
        this.letterCount = countLetters(source);
        this.sentenceCount = countSentences(source);

        int counted = 0;
        for (String token : source.toLowerCase().split(WORD_SEPARATOR)) {
            if (token.isEmpty()) {
                continue;
            }
            counted++;
            totalWordLength += token.length();
            if (token.length() > longestWord.length()) {
                longestWord = token;
            }
            if (removeStopWords && StopWords.contains(token)) {
                continue;
            }
            frequencies.merge(token, 1, Integer::sum);
        }
        this.totalWords = counted;
    }

    public boolean isStopWordsRemoved() {
        return stopWordsRemoved;
    }

    /** Every word in the text, stop words included: the length of the document. */
    public int getTotalWords() {
        return totalWords;
    }

    /** Distinct words among those that were counted into the frequency map. */
    public int getUniqueWords() {
        return frequencies.size();
    }

    public int getRankedWordCount() {
        int total = 0;
        for (int count : frequencies.values()) {
            total += count;
        }
        return total;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public int getLetterCount() {
        return letterCount;
    }

    public String getLongestWord() {
        return longestWord;
    }

    /** Words in the text divided by sentences in it. */
    public double averageSentenceLength() {
        if (sentenceCount == 0) {
            return 0.0;
        }
        return (double) totalWords / sentenceCount;
    }

    public double averageWordLength() {
        if (totalWords == 0) {
            return 0.0;
        }
        return (double) totalWordLength / totalWords;
    }

    /**
     * Unique words over total words. Close to 1 means almost nothing repeats,
     * which is normal for a short paragraph and unusual for a long document.
     */
    public double lexicalDiversity() {
        if (getRankedWordCount() == 0) {
            return 0.0;
        }
        return (double) getUniqueWords() / getRankedWordCount();
    }

    /** Words that occur exactly once. */
    public int hapaxCount() {
        int once = 0;
        for (int count : frequencies.values()) {
            if (count == 1) {
                once++;
            }
        }
        return once;
    }

    public int frequencyOf(String word) {
        return frequencies.getOrDefault(word.toLowerCase(), 0);
    }

    /**
     * The most frequent words, highest count first. A HashMap has no order, so
     * the entries are copied out and sorted: count descending, then the word
     * itself ascending so that ties come out in a stable, predictable order
     * instead of depending on hash layout.
     */
    public List<Map.Entry<String, Integer>> topWords(int limit) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(frequencies.entrySet());
        entries.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
                .thenComparing(Map.Entry::getKey));
        if (limit <= 0 || limit >= entries.size()) {
            return entries;
        }
        return new ArrayList<>(entries.subList(0, limit));
    }

    private static int countSentences(String text) {
        if (text.isBlank()) {
            return 0;
        }
        int sentences = 0;
        for (String piece : text.split(SENTENCE_SEPARATOR)) {
            if (!piece.isBlank()) {
                sentences++;
            }
        }
        // Text with no terminator at all is still one sentence.
        return sentences == 0 ? 1 : sentences;
    }

    private static int countLetters(String text) {
        int letters = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                letters++;
            }
        }
        return letters;
    }
}
