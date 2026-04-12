package com.oraclefusionhub.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinancialsMetadata {
    private static volatile FinancialsMetadata instance;

    private Snapshot snapshot;
    private Map<String, MetadataObject> objectMap;

    private FinancialsMetadata() {
        loadSnapshot();
    }

    public static FinancialsMetadata getInstance() {
        if (instance == null) {
            synchronized (FinancialsMetadata.class) {
                if (instance == null) {
                    instance = new FinancialsMetadata();
                }
            }
        }
        return instance;
    }

    private void loadSnapshot() {
        try (InputStream is = getClass().getResourceAsStream("/metadata/financials-latest.json")) {
            if (is == null) {
                System.err.println("Warning: Bundled metadata not found.");
                snapshot = new Snapshot();
                snapshot.objects = Collections.emptyList();
                objectMap = Collections.emptyMap();
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            snapshot = mapper.readValue(is, Snapshot.class);
            objectMap = new HashMap<>();
            if (snapshot.objects != null) {
                for (MetadataObject obj : snapshot.objects) {
                    if (obj.name != null) {
                        objectMap.put(obj.name.toUpperCase(), obj);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading metadata: " + e.getMessage());
            snapshot = new Snapshot();
            snapshot.objects = Collections.emptyList();
            objectMap = Collections.emptyMap();
        }
    }

    public List<MetadataObject> getObjects() {
        return snapshot.objects != null ? snapshot.objects : Collections.emptyList();
    }

    public MetadataObject getObject(String name) {
        if (name == null) return null;
        return objectMap.get(name.toUpperCase());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snapshot {
        public String product;
        public String release;
        public String sourceTocUrl;
        public String generatedAt;
        public List<MetadataObject> objects;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataObject {
        public String name;
        public String type; // TABLE or VIEW
        public String module;
        public String docPath;
        public String description;
        public List<Column> columns;
        public List<String> primaryKeys;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Column {
        public String name;
        public int ordinal;
        public String dataType;
        public boolean nullable;
        public String comment;
    }
}
