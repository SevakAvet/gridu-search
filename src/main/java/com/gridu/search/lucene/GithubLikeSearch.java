package com.gridu.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/***
 * Simple github-like search implementation
 * Based on Keyword analyzer, that doesn't do any tokenization preserving whole line from input as single token
 * RegEx query is used for matching, user's query is transformed into regex (see {@link GithubLikeSearch#generateRegex(String)})
 *
 * See {@link com.gridu.search.lucene.TestGithubLikeSearch} for examples
 */
public class GithubLikeSearch implements Closeable {
    public static final int DEFAULT_TOP_N = 5;
    public static final String CONTENT_FIELD = "content";

    private final IndexWriter writer;
    private IndexSearcher searcher;
    private IndexReader reader;
    private final Directory directory;
    private final Analyzer analyzer;

    public GithubLikeSearch() throws IOException {
        analyzer = new KeywordAnalyzer();

        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, config);
    }

    public void indexDocument(String filePath) throws IOException {
        for (String line : Files.readAllLines(Paths.get(filePath))) {
            Document document = new Document();
            document.add(new TextField(CONTENT_FIELD, line, Field.Store.YES));
            writer.addDocument(document);
        }
    }

    public void closeWriter() throws IOException {
        this.writer.close();
    }

    public List<Document> search(String text) throws IOException {
        return search(text, DEFAULT_TOP_N);
    }

    private void initReader() throws IOException {
        if (reader == null) {
            reader = DirectoryReader.open(directory);
        }

        if (searcher == null) {
            searcher = new IndexSearcher(reader);
        }
    }

    public List<Document> search(String text, int topN) throws IOException {
        initReader();

        RegexpQuery query = new RegexpQuery(new Term(CONTENT_FIELD, generateRegex(text)));
        TopDocs search = searcher.search(query, topN);

        List<Document> docs = new ArrayList<>();
        for (ScoreDoc scoreDoc : search.scoreDocs) {
            docs.add(reader.document(scoreDoc.doc));
        }

        return docs;
    }

    /***
     * Transforms input string into regex
     * appends asterix (.* - any symbol, zero or more occurrences) between each character of original string
     *
     * e.g.: "user" -> ".*u.*s.*e.*r.*"
     *
     * @param text user's query
     * @return regex, matching all subsequent characters
     */
    private String generateRegex(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(".*").append(c);
        }
        sb.append(".*");
        return sb.toString();
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
