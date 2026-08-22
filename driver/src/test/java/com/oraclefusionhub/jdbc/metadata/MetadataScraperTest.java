package com.oraclefusionhub.jdbc.metadata;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class MetadataScraperTest {

    @Test
    public void testScraper() throws Exception {
        MetadataScraper.DocumentFetcher mockFetcher = url -> {
            if (url.contains("toc.htm")) {
                return Jsoup.parse("<html><body><div><span>Financials</span><a href='MY_TABLE.htm'>MY_TABLE</a><a href='MY_VIEW_V.htm'>MY_VIEW_V</a></div></body></html>");
            } else if (url.contains("MY_TABLE")) {
                return Jsoup.parse("<html><body>" +
                        "<table>" +
                        "<tr><th>Name</th><th>Datatype</th><th>Null</th><th>Description</th></tr>" +
                        "<tr><td>COL_1</td><td>VARCHAR2(10)</td><td>Yes</td><td>Test Col 1</td></tr>" +
                        "<tr><td>COL_2</td><td>NUMBER</td><td>Not Null</td><td>Test Col 2</td></tr>" +
                        "</table>" +
                        "<p>Primary Key: COL_1, COL_2</p>" +
                        "</body></html>");
            } else if (url.contains("MY_VIEW_V")) {
                return Jsoup.parse("<html><body>" +
                        "<table>" +
                        "<tr><th>Name</th><th>Type</th><th>Null</th><th>Description</th></tr>" +
                        "<tr><td>V_COL_1</td><td>VARCHAR2(20)</td><td></td><td></td></tr>" +
                        "</table>" +
                        "</body></html>");
            }
            return Jsoup.parse("<html></html>");
        };

        MetadataScraper scraper = new MetadataScraper(mockFetcher);
        File tempOutput = File.createTempFile("metadata_test", ".json");
        tempOutput.deleteOnExit();

        scraper.runScraper("http://mock/toc.htm", "26B", tempOutput.getAbsolutePath());

        String json = new String(Files.readAllBytes(tempOutput.toPath()));
        assertTrue("Output should be valid JSON array", json.contains("["));
    }
}
