package org.atlasapi.equiv.results.probe;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class EquivalenceResultProbe {
    
    public static EquivalenceResultProbeBuilder equivalenceResultProbeFor(String canonicalUri) {
        return new EquivalenceResultProbeBuilder(checkNotNull(canonicalUri));
    }

    public static class EquivalenceResultProbeBuilder {

        private final String target;
        private Set<String> equivalent = ImmutableSet.of();
        private Set<String> notEquivalent = ImmutableSet.of();

        public EquivalenceResultProbeBuilder(String canonicalUri) {
            this.target = canonicalUri;
        }

        public EquivalenceResultProbeBuilder isEquivalentTo(Iterable<String> equivalents) {
            this.equivalent = ImmutableSet.copyOf(equivalents);
            return this;
        }

        public EquivalenceResultProbeBuilder isNotEquivalentTo(Iterable<String> notEquivalents) {
            this.notEquivalent = ImmutableSet.copyOf(notEquivalents);
            return this;
        }

        public EquivalenceResultProbe build() {
            return new EquivalenceResultProbe(target, equivalent, notEquivalent);
        }
    }

    private final String target;
    private final Set<String> equivalent;
    private final Set<String> notEquivalent;

    public EquivalenceResultProbe(String target, Set<String> equivalent, Set<String> notEquivalent) {
        this.target = target;
        this.equivalent = equivalent;
        this.notEquivalent = notEquivalent;
    }

    public String target() {
        return target;
    }

    public Set<String> expectedEquivalent() {
        return equivalent;
    }

    public Set<String> expectedNotEquivalent() {
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
