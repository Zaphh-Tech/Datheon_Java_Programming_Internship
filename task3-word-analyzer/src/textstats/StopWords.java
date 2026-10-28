package textstats;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The common English words that would otherwise take every slot in a top ten:
 * on ordinary prose "the", "and" and "of" beat anything that carries meaning.
 * Filtering them is optional, so the raw counts stay available.
 */
public final class StopWords {

    private static final Set<String> WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "all", "also", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "can", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "him", "his", "how",
            "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most", "my",
            "no", "nor", "not", "now", "of", "off", "on", "once", "only", "or", "other", "our", "out",
            "over", "own", "same", "she", "should", "so", "some", "such", "than", "that", "the",
            "their", "them", "then", "there", "these", "they", "this", "those", "through", "to", "too",
            "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which",
            "while", "who", "whom", "why", "will", "with", "would", "you", "your")));

    private StopWords() {
    }

    public static boolean contains(String word) {
        return WORDS.contains(word);
    }

    public static int size() {
        return WORDS.size();
    }
}
