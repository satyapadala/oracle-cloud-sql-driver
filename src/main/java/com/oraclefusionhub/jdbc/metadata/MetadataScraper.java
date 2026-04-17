package com.oraclefusionhub.jdbc.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.HttpStatusException;

public class MetadataScraper {

    public static void main(String[] args) throws Exception {
        List<String> tocUrls = new ArrayList<>();
        String release = "26B";
        String output = "src/main/resources/metadata/financials-latest.json";

        for (int i = 0; i < args.length; i++) {
            if ("--toc-url".equals(args[i]) && i + 1 < args.length) tocUrls.add(args[++i]);
            else if ("--release".equals(args[i]) && i + 1 < args.length) release = args[++i];
            else if ("--output".equals(args[i]) && i + 1 < args.length) output = args[++i];
        }

        // Default to financials + common applications if no explicit TOC URLs provided
        if (tocUrls.isEmpty()) {
            tocUrls.add("https://docs.oracle.com/en/cloud/saas/financials/26b/oedmf/toc.htm");
            tocUrls.add("https://docs.oracle.com/en/cloud/saas/applications-common/26b/oedma/toc.htm");
        }

        MetadataScraper scraper = new MetadataScraper(url -> {
            int retries = 5;
            long backoff = 2000;
            while(true) {
                try {
                    return Jsoup.connect(url).get();
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 429) {
                        if (--retries < 0) throw e;
                        System.out.println("Rate limited (429) fetching " + url + ". Retrying in " + backoff + "ms...");
                        Thread.sleep(backoff);
                        backoff *= 2; 
                    } else {
                        throw e;
                    }
                }
            }
        });
        scraper.runScraper(tocUrls, release, output);
    }

    public interface DocumentFetcher {
        Document fetch(String url) throws Exception;
    }

    private final DocumentFetcher fetcher;

    public MetadataScraper(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void runScraper(String tocUrl, String release, String output) throws Exception {
        runScraper(Collections.singletonList(tocUrl), release, output);
    }

    public void runScraper(List<String> tocUrls, String release, String output) throws Exception {
        FinancialsMetadata.Snapshot mergedSnapshot = new FinancialsMetadata.Snapshot();
        mergedSnapshot.product = "oracle-fusion";
        mergedSnapshot.release = release;
        mergedSnapshot.sourceTocUrls = tocUrls;
        mergedSnapshot.sourceTocUrl = tocUrls.get(0); // backward compat
        mergedSnapshot.generatedAt = Instant.now().toString();
        mergedSnapshot.objects = new ArrayList<>();

        Map<String, FinancialsMetadata.MetadataObject> globalObjectMap = new HashMap<>();

        for (String tocUrl : tocUrls) {
            System.out.println("Starting scraper with TOC: " + tocUrl);
            scrapeOneToc(tocUrl, globalObjectMap);
        }

        mergedSnapshot.objects.addAll(globalObjectMap.values());
        mergedSnapshot.objects.sort(Comparator.comparing(o -> o.name));

        File outFile = new File(output);
        outFile.getParentFile().mkdirs();

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(outFile, mergedSnapshot);

        System.out.println("Scraping completed. Generated " + outFile.getAbsolutePath());
    }

    private void scrapeOneToc(String tocUrl, Map<String, FinancialsMetadata.MetadataObject> globalObjectMap) throws Exception {

        Document tocDoc;
        try {
            tocDoc = fetcher.fetch(tocUrl);
        } catch (Exception e) {
            System.err.println("Could not fetch TOC: " + e.getMessage());
            return;
        }

        URI baseUri = new URI(tocUrl);
        Elements links = tocDoc.select("a[href]");
        Map<String, FinancialsMetadata.MetadataObject> localObjectMap = new HashMap<>();

        for (Element link : links) {
            String href = link.attr("href");
            String text = link.text().trim();

            if (text.matches("^[A-Z0-9_]+$") && text.length() > 2) {
                if (!localObjectMap.containsKey(text) && !globalObjectMap.containsKey(text)) {
                    FinancialsMetadata.MetadataObject obj = new FinancialsMetadata.MetadataObject();
                    obj.name = text;
                    try {
                        obj.docPath = baseUri.resolve(href).toString();
                    } catch (Exception e) {
                        obj.docPath = href;
                    }
                    obj.type = text.endsWith("_V") || text.endsWith("_VL") ? "VIEW" : "TABLE";
                    obj.module = extractModule(link);
                    localObjectMap.put(text, obj);
                }
            }
        }

        int total = localObjectMap.size();
        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<FinancialsMetadata.MetadataObject> syncObjects = Collections.synchronizedList(new ArrayList<>());

        for (FinancialsMetadata.MetadataObject obj : localObjectMap.values()) {
            executor.submit(() -> {
                try {
                    int current = count.incrementAndGet();
                    if (current % 50 == 0 || current == 1 || current == total) {
                        System.out.println("Fetching: " + obj.name + " (" + current + "/" + total + ")");
                    }
                    Document doc = fetcher.fetch(obj.docPath);
                    parseObjectPage(doc, obj);
                } catch (Exception e) {
                    System.err.println("Failed to fetch/parse " + obj.docPath + ": " + e.getMessage());
                    obj.columns = new ArrayList<>();
                    obj.primaryKeys = new ArrayList<>();
                }
                syncObjects.add(obj);
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.HOURS);

        for (FinancialsMetadata.MetadataObject obj : syncObjects) {
            globalObjectMap.put(obj.name, obj);
        }
    }

    private String extractModule(Element link) {
        Element parent = link.parent();
        while (parent != null) {
            String text = parent.ownText();
            if (text != null && !text.isEmpty() && !text.equals(link.text())) {
                return text.trim();
            }
            parent = parent.parent();
        }
        return "Financials";
    }

    private void parseObjectPage(Document doc, FinancialsMetadata.MetadataObject obj) {
        obj.columns = new ArrayList<>();
        obj.primaryKeys = new ArrayList<>();
        
        for (Element p : doc.select("div.body > p.p")) {
            String text = p.text().trim();
            if (!text.isEmpty()) {
                obj.description = text;
                break;
            }
        }
        
        Elements tables = doc.select("table[summary='Columns']");
        if (tables.isEmpty()) {
            tables = doc.select("table");
        }
        int ordinal = 1;
        for (Element table : tables) {
            Elements ths = table.select("th");
            boolean hasName = false;
            int nameIdx = -1, typeIdx = -1, nullIdx = -1, descIdx = -1;
            
            for (int i = 0; i < ths.size(); i++) {
                String header = ths.get(i).text().toLowerCase();
                if (header.contains("name")) { nameIdx = i; hasName = true; }
                else if (header.contains("type") || header.contains("datatype")) typeIdx = i;
                else if (header.contains("null")) nullIdx = i;
                else if (header.contains("description") || header.contains("comment")) descIdx = i;
            }
            
            boolean isColumnTable = hasName && (typeIdx >= 0 || descIdx >= 0 || ths.size() == 1);
            
            if (isColumnTable) {
                Elements rows = table.select("tr");
                boolean isViewFormat = (ths.size() == 1 && nameIdx == 0 && typeIdx == -1);

                if (isViewFormat) {
                    for (Element row : rows) {
                        Elements tds = row.select("td");
                        if (tds.size() == 1) {
                            Elements ps = tds.get(0).select("p");
                            for (Element p : ps) {
                                String text = p.text().trim();
                                if (text.matches("^[A-Z0-9_]+$")) {
                                    FinancialsMetadata.Column col = new FinancialsMetadata.Column();
                                    col.name = text;
                                    col.ordinal = ordinal++;
                                    col.dataType = "VARCHAR2";
                                    col.nullable = true;
                                    obj.columns.add(col);
                                } else if (!text.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    for (Element row : rows) {
                        Elements tds = row.select("td");
                        if (tds.size() > nameIdx && nameIdx >= 0) {
                            FinancialsMetadata.Column col = new FinancialsMetadata.Column();
                            col.name = tds.get(nameIdx).text().trim();
                            if (col.name.isEmpty()) continue;
                            col.ordinal = ordinal++;
                            if (typeIdx >= 0 && typeIdx < tds.size()) col.dataType = tds.get(typeIdx).text().trim();
                            if (nullIdx >= 0 && nullIdx < tds.size()) {
                                String nl = tds.get(nullIdx).text().trim().toLowerCase();
                                col.nullable = !nl.contains("not null");
                            } else {
                                col.nullable = true;
                            }
                            if (descIdx >= 0 && descIdx < tds.size()) col.comment = tds.get(descIdx).text().trim();
                            
                            obj.columns.add(col);
                        }
                    }
                }
                break; 
            }
        }
        
        Elements contentNodes = doc.select("p, div, li, strong");
        for (Element p : contentNodes) {
            String text = p.text();
            if (text.toLowerCase().contains("primary key")) {
                String remainder = text.substring(text.toLowerCase().indexOf("primary key"));
                if (remainder.contains(":")) {
                    String keysStr = remainder.substring(remainder.indexOf(":") + 1).trim();
                    String[] keys = keysStr.split(",");
                    for (String k : keys) {
                        String cleanKey = k.trim().replaceAll("[^A-Za-z0-9_]", "");
                        if (!cleanKey.isEmpty() && cleanKey.matches("^[A-Z0-9_]+$")) {
                            if (!obj.primaryKeys.contains(cleanKey)) {
                                obj.primaryKeys.add(cleanKey);
                            }
                        }
                    }
                }
            }
        }
    }
}
