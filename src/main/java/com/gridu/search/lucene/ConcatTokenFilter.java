package com.gridu.search.lucene;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ConcatTokenFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    private final String delimiter;

    protected ConcatTokenFilter(TokenStream input, String delimiter) {
        super(input);
        this.delimiter = delimiter;
    }

    @Override
    public boolean incrementToken() throws IOException {
        List<String> words = new ArrayList<>();
        while (input.incrementToken()) {
            String term = new String(termAttr.buffer(), 0, termAttr.length());
            words.add(term);
        }

        if (words.isEmpty()) {
            return false;
        }

        String result = String.join(delimiter, words);
        if (!result.isEmpty()) {
            termAttr.copyBuffer(result.toCharArray(), 0, result.length());
            termAttr.setLength(result.length());
            return true;
        }

        return false;
    }
}