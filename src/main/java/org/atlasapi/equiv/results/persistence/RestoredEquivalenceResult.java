package org.atlasapi.equiv.results.persistence;

import com.google.common.collect.Table;

public class RestoredEquivalenceResult {

    private final String id;
    private final String title;
    private final Table<EquivalenceIdentifier, String, Double> results;

    public RestoredEquivalenceResult(String targetId, String targetTitle, Table<EquivalenceIdentifier, String, Double> results) {
        this.id = targetId;
        this.title = targetTitle;
        this.results = results;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public Table<EquivalenceIdentifier, String, Double> results() {
        return results;
    }
    
}
