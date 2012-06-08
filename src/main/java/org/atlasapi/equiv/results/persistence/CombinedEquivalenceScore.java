package org.atlasapi.equiv.results.persistence;

import java.io.Serializable;

import com.google.common.base.Objects;


public class CombinedEquivalenceScore implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String title;
    private final Double score;
    private final boolean strong;
    private final String publisher;

    public CombinedEquivalenceScore(String id, String title, Double score, boolean strong, String publisher) {
        this.id = id;
        this.title = title;
        this.score = score;
        this.strong = strong;
        this.publisher = publisher;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public Double score() {
        return score;
    }

    public boolean strong() {
        return strong;
    }

    public String publisher() {
        return publisher;
    }   
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, title, publisher, strong);
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof CombinedEquivalenceScore) {
            CombinedEquivalenceScore other = (CombinedEquivalenceScore) that;
            return id.equals(other.id) && title.equals(other.title) && publisher.equals(other.publisher) && strong == other.strong;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) %s", title, id, strong);
    }
}
