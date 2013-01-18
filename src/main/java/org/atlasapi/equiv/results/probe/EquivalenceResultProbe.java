package org.atlasapi.equiv.results.probe;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.media.common.Id;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class EquivalenceResultProbe {
    
    public static EquivalenceResultProbeBuilder equivalenceResultProbeFor(Id id) {
        return new EquivalenceResultProbeBuilder(checkNotNull(id));
    }

    public static class EquivalenceResultProbeBuilder {

        private final Id target;
        private Set<Id> equivalent = ImmutableSet.of();
        private Set<Id> notEquivalent = ImmutableSet.of();

        public EquivalenceResultProbeBuilder(Id id) {
            this.target = id;
        }

        public EquivalenceResultProbeBuilder isEquivalentTo(Iterable<Id> equivalents) {
            this.equivalent = ImmutableSet.copyOf(equivalents);
            return this;
        }

        public EquivalenceResultProbeBuilder isNotEquivalentTo(Iterable<Id> notEquivalents) {
            this.notEquivalent = ImmutableSet.copyOf(notEquivalents);
            return this;
        }

        public EquivalenceResultProbe build() {
            return new EquivalenceResultProbe(target, equivalent, notEquivalent);
        }
    }

    private final Id target;
    private final Set<Id> equivalent;
    private final Set<Id> notEquivalent;

    public EquivalenceResultProbe(Id target, Set<Id> equivalent, Set<Id> notEquivalent) {
        this.target = target;
        this.equivalent = equivalent;
        this.notEquivalent = notEquivalent;
    }

    public Id target() {
        return target;
    }

    public Set<Id> expectedEquivalent() {
        return equivalent;
    }

    public Set<Id> expectedNotEquivalent() {
        return notEquivalent;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if(that instanceof EquivalenceResultProbe) {
            EquivalenceResultProbe other = (EquivalenceResultProbe) that;
            return target.equals(other.target);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(target);
    }
    
    @Override
    public String toString() {
        return String.format("Equivalence Result Probe for %s", target);
    }
}
