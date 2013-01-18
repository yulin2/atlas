package org.atlasapi.equiv.results.persistence;

import java.io.Serializable;
import java.util.List;

import org.atlasapi.media.common.Id;
import org.joda.time.DateTime;

import com.google.common.base.Objects;
import com.google.common.collect.Table;

public class StoredEquivalenceResult implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String title;
    private final Table<Id, String, Double> results;
    private final List<CombinedEquivalenceScore> totals;
    private final DateTime resultTime;
    private final List<Object> desc;

    public StoredEquivalenceResult(String targetId, String targetTitle, Table<Id, String, Double> results, List<CombinedEquivalenceScore> totals, DateTime resultTime, List<Object> desc) {
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

    public Table<Id, String, Double> sourceResults() {
        return results;
    }

    public List<CombinedEquivalenceScore> combinedResults() {
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
        if(that instanceof StoredEquivalenceResult) {
            StoredEquivalenceResult other = (StoredEquivalenceResult) that;
            return id.equals(other.id) 
                && title.equals(other.title) 
                && results.equals(other.results) 
                && totals.equals(other.totals) 
                && resultTime.equals(resultTime);
        }
        return super.equals(that);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, title, results, totals, resultTime);
    }

}
