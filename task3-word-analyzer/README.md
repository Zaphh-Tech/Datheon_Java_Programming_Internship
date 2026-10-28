# Task 3: Word Analyzer

A text file processor. It reads a document, cleans it, and reports length,
sentence and word averages, and the most frequent words.

## Running it

    cd task3-word-analyzer
    javac -d out src/textstats/*.java
    java -cp out textstats.WordAnalyzer ../data/sample_article.txt

    java -cp out textstats.WordAnalyzer ../data/sample_article.txt -n 15
    java -cp out textstats.WordAnalyzer ../data/sample_article.txt --keep-stopwords
    cat notes.txt | java -cp out textstats.WordAnalyzer -

| Option | Meaning |
| --- | --- |
| `file` | The file to analyse, or `-` for standard input. Defaults to `../data/sample_article.txt`. |
| `-n count` | How many words to rank. Default 10. |
| `--keep-stopwords` | Rank common English words as well. |
| `-h`, `--help` | Usage. |

It exits with status 1 if the file cannot be read or an option is wrong, so it
can be used from a script.

## Classes

| File | Responsibility |
| --- | --- |
| `TextStatistics.java` | Tokenises the text once in the constructor, then answers every question from the frequency map. |
| `StopWords.java` | The 115 common English words that are filtered out of the ranking by default. |
| `WordAnalyzer.java` | Reads the file with a `BufferedReader`, parses the options, prints the two tables. |

## How the text is cleaned

Tokens are split on `\W+`, which treats every character that is not a letter,
digit or underscore as a separator. That is what strips the punctuation:
`"code,"` and `"code."` both become `code`. Case is folded to lower, so
`Data` and `data` are the same word.

Sentences are counted by splitting on `[.!?]+` and keeping the non blank
pieces. Text with no terminator at all still counts as one sentence rather
than zero, because the average sentence length would otherwise divide by zero.

`Total words` counts every word in the document, stop words included, because
it is the length of the document. `Unique words` counts the distinct words that
made it into the ranking. Those two are deliberately measured on different
sets, and the summary labels them accordingly.

Stop words are filtered from the ranking by default. On ordinary prose the top
ten is otherwise "the, and, of, to, a" every time, which says nothing about the
document. `--keep-stopwords` turns the filter off and ranks the raw counts.

Ties are broken alphabetically. A `HashMap` has no order, so without a
tie break the ranking of three words that all occur twice would depend on hash
layout and could change between runs.

## Sample output

    java -cp out textstats.WordAnalyzer ../data/sample_article.txt

    ----------------------------------------------------------------
    TEXT SUMMARY
    ----------------------------------------------------------------
    Source                   ../data/sample_article.txt
    Lines                    1
    Characters               376
    Letters                  324
    Sentences                3
    Total words              46
    Unique words             35
    Words used once          32
    Average sentence         15.33 words
    Average word             7.04 characters
    Lexical diversity        0.92
    Longest word             practitioners (13 characters)
    ----------------------------------------------------------------
    TOP 10 WORDS (common English words filtered out)
    ----------------------------------------------------------------
    RANK  WORD                  COUNT   SHARE
       1  data                      2   5.26%  ##############################
       2  modern                    2   5.26%  ##############################
       3  software                  2   5.26%  ##############################
       4  architectures             1   2.63%  ###############
       5  artificial                1   2.63%  ###############
       6  automation                1   2.63%  ###############
       7  build                     1   2.63%  ###############
       8  clean                     1   2.63%  ###############
       9  cloud                     1   2.63%  ###############
      10  code                      1   2.63%  ###############
    ----------------------------------------------------------------
    115 stop words are excluded from the ranking. Run with --keep-stopwords to include them.

Same file with the filter off, five words:

    TOP 5 WORDS (nothing filtered)
    ----------------------------------------------------------------
    RANK  WORD                  COUNT   SHARE
       1  and                       3   6.52%  ##############################
       2  data                      2   4.35%  ####################
       3  modern                    2   4.35%  ####################
       4  software                  2   4.35%  ####################
       5  architectures             1   2.17%  ##########

That difference is the reason the filter exists.

## Reading the numbers

`Lexical diversity` is unique words over counted words. The sample paragraph
scores 0.92, which means almost nothing repeats. That is normal for a few
sentences of marketing prose and would be unusual in a long document, where
the figure drops as common vocabulary comes round again.

`Words used once` is the count of words that appear exactly once. On this
sample it is 32 of 35 distinct words, which says the same thing from the other
direction.
