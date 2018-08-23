package com.gridu.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class TestConcatAnalyzer {

    @Test
    public void testConcat() throws IOException {
        Analyzer analyzer = new ConcatAnalyzer("|");
        RAMDirectory directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        List<String> input = Arrays.asList("hello world", "how are you doing", "london is the capital of great britain");

        for (String str : input) {
            Document document = new Document();
            document.add(new TextField("content", str, Field.Store.YES));
            writer.addDocument(document);
        }

        writer.close();

        DirectoryReader indexReader = DirectoryReader.open(directory);
        List<String> terms = readAllTerms(indexReader);

        Assert.assertThat(terms, containsInAnyOrder("hello|world", "how|you|doing", "london|capital|great|britain"));
    }

    private List<String> readAllTerms(DirectoryReader indexReader) throws IOException {
        List<String> terms = new ArrayList<>();
        for (LeafReaderContext leafReaderContext : indexReader.getContext().leaves()) {
            LeafReader reader = leafReaderContext.reader();

            TermsEnum termsEnum = reader.terms("content").iterator();
            BytesRef nextTerm = termsEnum.next();
            while (nextTerm != null) {
                String term = termsEnum.term().utf8ToString();
                terms.add(term);
                nextTerm = termsEnum.next();
            }
        }
        return terms;
    }
}
