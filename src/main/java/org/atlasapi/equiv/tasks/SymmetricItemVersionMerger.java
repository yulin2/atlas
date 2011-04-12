package org.atlasapi.equiv.tasks;

import java.util.HashSet;
import java.util.Set;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SymmetricItemVersionMerger {
    
    public void mergeVersions(Item subject, Iterable<Item> equivalents) {
        for (Item equiv : equivalents) {
            
            // remove previously merged versions from each item
            Set<Version> subjectVersions = Sets.newHashSet(Iterables.filter(subject.getVersions(), Predicates.not(isProvidedBy(equiv.getPublisher()))));
            Set<Version> equivVersions = Sets.newHashSet(Iterables.filter(equiv.getVersions(), Predicates.not(isProvidedBy(subject.getPublisher()))));
            
            HashSet<Version> versions = Sets.newHashSet(Iterables.concat(subjectVersions, equivVersions));
            subject.setVersions(versions);
            equiv.setVersions(versions);
            
            subject.addEquivalentTo(equiv);
            equiv.addEquivalentTo(subject);
        }
    }

    private static Predicate<Version> isProvidedBy(final Publisher publisher) {
        return new Predicate<Version>() {
            @Override
            public boolean apply(Version input) {
                return input.getProvider() != null && input.getProvider().equals(publisher);
            }
        };
    }
    
}
