package com.gridu.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.xml.builders.SpanNearBuilder;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilderFactory;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.NearSpansOrdered;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/***
 * Proximity search implementation based on phrase query
 */
public class ProximitySearch implements Closeable {
    public static final int DEFAULT_SLOP = 5;
    public static final String CONTENT_FIELD = "content";
    public static final String FILE_NAME_FIELD = "fileName";

    private final IndexWriter writer;
    private IndexSearcher searcher;
    private IndexReader reader;
    private final Directory directory;
    private final Analyzer analyzer;

    public ProximitySearch() throws IOException {
        analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, config);
    }

    public void indexDocument(String filePath) throws IOException {
        for (String line : Files.readAllLines(Paths.get(filePath))) {
            String[] split = line.split("=");
            String fileName = split[0];
            String content = split[1].substring(1, split[1].length() - 1);

            Document document = new Document();
            document.add(new TextField(CONTENT_FIELD, content, Field.Store.NO));
            document.add(new TextField(FILE_NAME_FIELD, fileName, Field.Store.YES));
            writer.addDocument(document);
        }
    }

    public void closeWriter() throws IOException {
        this.writer.close();
    }

    private void initReader() throws IOException {
        if (reader == null) {
            reader = DirectoryReader.open(directory);
        }

        if (searcher == null) {
            searcher = new IndexSearcher(reader);
        }
    }

    public List<Document> searchWithPrefix(String text, int slop) throws IOException {
        initReader();

        MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder()
                .setSlop(slop);

        String[] words = text.split("\\W+");
        for (int i = 0; i < words.length - 1; i++) {
            builder.add(new Term(CONTENT_FIELD, words[i]));
        }

        String lastWord = words[words.length - 1];
        Set<String> termsWithPrefix = new HashSet<>();

        for (LeafReaderContext leafReaderContext : reader.getContext().leaves()) {
            LeafReader reader = leafReaderContext.reader();

            TermsEnum termsEnum = reader.terms(CONTENT_FIELD).iterator();
            BytesRef nextTerm = termsEnum.next();
            while (nextTerm != null) {
                String term = termsEnum.term().utf8ToString();
                if (term.startsWith(lastWord)) {
                    termsWithPrefix.add(term);
                }

                nextTerm = termsEnum.next();
            }
        }

        Term[] prefixTerms = termsWithPrefix.stream().map(term -> new Term(CONTENT_FIELD, term)).toArray(Term[]::new);
        MultiPhraseQuery query = builder
                .add(prefixTerms)
                .build();

        TopDocs search = searcher.search(query, reader.numDocs());

        List<Document> docs = new ArrayList<>();
        for (ScoreDoc scoreDoc : search.scoreDocs) {
            docs.add(reader.document(scoreDoc.doc));
        }

        return docs;
    }

    public List<Document> search(String text, int slop) throws IOException {
        initReader();

        PhraseQuery.Builder builder = new PhraseQuery.Builder()
                .setSlop(slop);

        Arrays.stream(text.split("\\W+"))
                .map(x -> new Term(CONTENT_FIELD, x))
                .forEach(builder::add);

        PhraseQuery query = builder.build();

        TopDocs search = searcher.search(query, reader.numDocs());

        List<Document> docs = new ArrayList<>();
        for (ScoreDoc scoreDoc : search.scoreDocs) {
            docs.add(reader.document(scoreDoc.doc));
        }

        return docs;
    }

    public List<Document> searchWithStrictOrder(String text, int slop) throws IOException {
        initReader();

        SpanNearQuery.Builder builder = new SpanNearQuery.Builder(CONTENT_FIELD, true)
                .setSlop(slop);

        Arrays.stream(text.split("\\W+"))
                .map(x -> new SpanTermQuery(new Term(CONTENT_FIELD, x)))
                .forEach(builder::addClause);

        SpanNearQuery query = builder.build();

        TopDocs search = searcher.search(query, reader.numDocs());

        List<Document> docs = new ArrayList<>();
        for (ScoreDoc scoreDoc : search.scoreDocs) {
            docs.add(reader.document(scoreDoc.doc));
        }

        return docs;
    }

    @Override
    public void close() throws IOException {
        analyzer.close();
        directory.close();

        if (reader != null) {
            reader.close();
        }
    }
}
