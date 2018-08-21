package com.gridu.search.lucene;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.*;

import static com.gridu.search.lucene.GithubLikeSearch.CONTENT_FIELD;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(Parameterized.class)
public class TestGithubLikeSearch {
    private static GithubLikeSearch githubLikeSearch;

    private String query;
    private List<String> expectecResult;

    public TestGithubLikeSearch(String query, List<String> expectecResult) {
        this.query = query;
        this.expectecResult = expectecResult;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"lqdocspg", Collections.singletonList("lucene/queryparser/docs/xml/img/plus.gif")},
                {"minusbottom.gif", Collections.singletonList("lucene/queryparser/docs/xml/img/minusbottom.gif")},
                {"lqd///gif", Arrays.asList(
                        "lucene/queryparser/docs/xml/img/plus.gif",
                        "lucene/queryparser/docs/xml/img/join.gif",
                        "lucene/queryparser/docs/xml/img/minusbottom.gif"
                )},
                {"lucene", Arrays.asList(
                        "lucene/queryparser/docs/xml/img/plus.gif",
                        "lucene/queryparser/docs/xml/img/join.gif",
                        "lucene/queryparser/docs/xml/img/minusbottom.gif"
                )}
        });
    }

    @BeforeClass
    public static void setup() throws IOException {
        githubLikeSearch = new GithubLikeSearch();
        githubLikeSearch.indexDocument(TestGithubLikeSearch.class.getResource("/githublike/input.txt").getFile());
        githubLikeSearch.closeWriter();
    }

    @AfterClass
    public static void close() throws IOException {
        githubLikeSearch.close();
    }


    @Test
    public void testSearch() throws IOException {
        List<Document> result = githubLikeSearch.search(this.query);
        Assert.assertEquals(this.expectecResult.size(), result.size());

        List<String> resultString = new ArrayList<>();
        for (Document document : result) {
            resultString.add(document.get(CONTENT_FIELD));
        }

        Assert.assertThat(this.expectecResult, containsInAnyOrder(resultString.toArray()));
    }
}
