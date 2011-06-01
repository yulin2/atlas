package org.atlasapi.equiv.results.persistence;

import com.google.common.base.Objects;


public class EquivalenceIdentifier {

    private final String id;
    private final String title;
    private final boolean strong;
    private final String publisher;

    public EquivalenceIdentifier(String id, String title, boolean strong, String publisher) {
        this.id = id;
        this.title = title;
        this.strong = strong;
        this.publisher = publisher;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
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
        if(that instanceof EquivalenceIdentifier) {
            EquivalenceIdentifier other = (EquivalenceIdentifier) that;
            return id.equals(other.id) && title.equals(other.title) && publisher.equals(other.publisher) && strong == other.strong;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) %s", title, id, strong);
    }
}
