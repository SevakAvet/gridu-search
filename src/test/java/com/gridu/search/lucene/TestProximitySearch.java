package com.gridu.search.lucene;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(JUnitParamsRunner.class)
public class TestProximitySearch {
    private static ProximitySearch proximitySearch;

    public static Collection<Object[]> searchData() {
        return Arrays.asList(new Object[][]{
                {"to be not", 1, Collections.singletonList("file1")},
                {"to be to", 1, Collections.emptyList()},
                {"to", 1, Arrays.asList("file1", "file3")},
                {"long story short", 0, Collections.singletonList("file2")},
                {"long short", 0, Collections.emptyList()},
                {"long short", 1, Collections.singletonList("file2")},
                {"story long", 1, Collections.emptyList()},
                {"story long", 2, Arrays.asList("file2", "file4")}
        });
    }

    public static Collection<Object[]> searchDataPrefix() {
        return Arrays.asList(new Object[][]{
                {"long story sho", 0, Collections.singletonList("file2")},
                {"long story very sho", 1, Collections.singletonList("file4")},
        });
    }

    public static Collection<Object[]> searchDataStrictOrder() {
        return Arrays.asList(new Object[][]{
                {"long story", 0, Arrays.asList("file2", "file4")},
                {"make story", 2, Collections.singletonList("file2")},
                {"story long", 1, Collections.emptyList()},
        });
    }


    @BeforeClass
    public static void setup() throws IOException {
        proximitySearch = new ProximitySearch();
        proximitySearch.indexDocument(TestProximitySearch.class.getResource("/proximity/input.txt").getFile());
        proximitySearch.closeWriter();
    }

    @AfterClass
    public static void close() throws IOException {
        proximitySearch.close();
    }

    @Test
    @Parameters(method = "searchData")
    public void testSearch(String query, int slop, List<String> expectedResult) throws IOException {
        search(expectedResult, proximitySearch.search(query, slop));
    }

    @Test
    @Parameters(method = "searchDataPrefix")
    public void testSearchPrefix(String query, int slop, List<String> expectedResult) throws IOException {
        search(expectedResult, proximitySearch.searchWithPrefix(query, slop));
    }

    @Test
    @Parameters(method = "searchDataStrictOrder")
    public void testSearchStrictOrder(String query, int slop, List<String> expectedResult) throws IOException {
        search(expectedResult, proximitySearch.searchWithStrictOrder(query, slop));
    }

    private void search(List<String> expectedResult, List<Document> result) {
        Assert.assertEquals(expectedResult.size(), result.size());

        List<String> resultString = new ArrayList<>();
        for (Document document : result) {
            resultString.add(document.get(ProximitySearch.FILE_NAME_FIELD));
        }

        Assert.assertThat(expectedResult, containsInAnyOrder(resultString.toArray()));
    }
}
