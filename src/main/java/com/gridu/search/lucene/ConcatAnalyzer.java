package com.gridu.search.lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;

import java.util.Arrays;
import java.util.List;

public class ConcatAnalyzer extends StopwordAnalyzerBase {
    public static final CharArraySet ENGLISH_STOP_WORDS_SET;
    public static final String DEFAULT_DELIMITER = " ";

    static {
        final List<String> stopWords = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"
        );
        final CharArraySet stopSet = new CharArraySet(stopWords, false);
        ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }

    private String delimiter;

    public ConcatAnalyzer(String delimiter) {
        super(ENGLISH_STOP_WORDS_SET);
        this.delimiter = delimiter;
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();

        TokenStream stream = new StandardFilter(tokenizer);
        stream = new LowerCaseFilter(stream);
        stream = new StopFilter(stream, stopwords);
        stream = new ConcatTokenFilter(stream, delimiter);

        return new TokenStreamComponents(tokenizer, stream);
    }
}
