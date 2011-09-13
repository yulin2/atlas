package org.atlasapi.equiv.results.persistence;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.Objects;
import com.google.common.collect.Table;

public class RestoredEquivalenceResult {

    private final String id;
    private final String title;
    private final Table<String, String, Double> results;
    private final Map<EquivalenceIdentifier, Double> totals;
    private final DateTime resultTime;
    private final List<Object> desc;

    public RestoredEquivalenceResult(String targetId, String targetTitle, Table<String, String, Double> results, Map<EquivalenceIdentifier, Double> totals, DateTime resultTime, List<Object> desc) {
        this.id = targetId;
        this.title = targetTitle;
        this.results = results;
        this.totals = totals;
        this.resultTime = resultTime;
        this.desc = desc;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public Table<String, String, Double> sourceResults() {
        return results;
    }

    public Map<EquivalenceIdentifier, Double> combinedResults() {
        return totals;
    }

    public DateTime resultTime() {
        return resultTime;
    }

    public List<Object> description() {
        return desc;
    }
    
    @Override
    public String toString() {
        return String.format("Result for %s %s", title, id);
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof RestoredEquivalenceResult) {
            RestoredEquivalenceResult other = (RestoredEquivalenceResult) that;
            return id.equals(other.id) && title.equals(other.title) && results.equals(other.results) && totals.equals(other.totals) && resultTime.equals(resultTime);
        }
        return super.equals(that);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, title, results, totals, resultTime);
    }

}
