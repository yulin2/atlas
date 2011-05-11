package org.atlasapi.equiv.results.persistence;

import java.util.Map;

import com.google.common.collect.Table;

public class RestoredEquivalenceResult {

    private final String id;
    private final String title;
    private final Table<EquivalenceIdentifier, String, Double> results;
    private final Map<EquivalenceIdentifier, Double> totals;

    public RestoredEquivalenceResult(String targetId, String targetTitle, Table<EquivalenceIdentifier, String, Double> results, Map<EquivalenceIdentifier, Double> totals) {
        this.id = targetId;
        this.title = targetTitle;
        this.results = results;
        this.totals = totals;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public Table<EquivalenceIdentifier, String, Double> sourceResults() {
        return results;
    }

    public Map<EquivalenceIdentifier, Double> combinedResults() {
        return totals;
    }
    
}
