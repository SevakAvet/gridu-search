package com.gridu.search.lucene;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(Enclosed.class)
public class TestProximitySearch {

    @RunWith(Parameterized.class)
    public static class TestProximitySearchSimple {
        private static ProximitySearch proximitySearch;

        private String query;
        private int slop;
        private List<String> expectedResult;

        public TestProximitySearchSimple(String query, int slop, List<String> expectedResult) {
            this.query = query;
            this.slop = slop;
            this.expectedResult = expectedResult;
        }

        @Parameters
        public static Collection<Object[]> data() {
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
        public void testSearch() throws IOException {
            List<Document> result = proximitySearch.search(this.query, this.slop);
            Assert.assertEquals(this.expectedResult.size(), result.size());

            List<String> resultString = new ArrayList<>();
            for (Document document : result) {
                resultString.add(document.get(ProximitySearch.FILE_NAME_FIELD));
            }

            Assert.assertThat(this.expectedResult, containsInAnyOrder(resultString.toArray()));
        }
    }

    @RunWith(Parameterized.class)
    public static class TestProximitySearchPrefix {
        private static ProximitySearch proximitySearch;

        private String query;
        private int slop;
        private List<String> expectedResult;

        public TestProximitySearchPrefix(String query, int slop, List<String> expectedResult) {
            this.query = query;
            this.slop = slop;
            this.expectedResult = expectedResult;
        }

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"long story sho", 0, Collections.singletonList("file2")},
                    {"long story very sho", 1, Arrays.asList("file4")},
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
        public void testSearchWithPrefix() throws IOException {
            List<Document> result = proximitySearch.searchWithPrefix(this.query, this.slop);
            Assert.assertEquals(this.expectedResult.size(), result.size());

            List<String> resultString = new ArrayList<>();
            for (Document document : result) {
                resultString.add(document.get(ProximitySearch.FILE_NAME_FIELD));
            }

            Assert.assertThat(this.expectedResult, containsInAnyOrder(resultString.toArray()));
        }
    }

}
