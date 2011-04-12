package org.atlasapi.equiv.tasks;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

import java.util.Collection;
import java.util.Set;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class EquivCleaner {

    private final ContentResolver resolver;

    public EquivCleaner(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public Set<Identified> cleanEquivalences(Content content, Collection<? extends Content> knownEquivalents, Set<Publisher> targetPublishers) {

        Set<Identified> toWrite = Sets.newHashSet();

        Set<String> knownEquivUris = ImmutableSet.copyOf(Iterables.transform(knownEquivalents, Identified.TO_URI));

        for (String equivUri : ImmutableSet.copyOf(Iterables.filter(content.getEquivalentTo(), not(in(knownEquivUris))))) {

            Identified equivalent = resolver.findByCanonicalUri(equivUri);
            if (equivalent instanceof Content && targetPublishers.contains(((Content) equivalent).getPublisher())) {
                if (content instanceof Item) {
                    toWrite.add(cleanEquivalence((Item) content, (Item) equivalent));
                } else {
                    toWrite.add(cleanEquivalence((Container<?>) content, (Content) equivalent));
                }
            }

        }

        return toWrite;
    }

    private Identified cleanEquivalence(Content content, Content equivalent) {
        content.getEquivalentTo().remove(equivalent.getCanonicalUri());
        equivalent.getEquivalentTo().remove(content.getCanonicalUri());
        return equivalent;
    }

    private Identified cleanEquivalence(Item item, Item equivalent) {
        removeVersions(item, equivalent);
        cleanEquivalence((Content) item, equivalent);
        return equivalent.getFullContainer() == null ? equivalent : equivalent.getFullContainer();
    }

    private void removeVersions(Item item, Item equivalent) {
        item.setVersions(ImmutableSet.copyOf(Iterables.filter(item.getVersions(), not(isProvidedBy(equivalent.getPublisher())))));
        equivalent.setVersions(ImmutableSet.copyOf(Iterables.filter(equivalent.getVersions(), not(isProvidedBy(item.getPublisher())))));
    }

    private Predicate<Version> isProvidedBy(final Publisher publisher) {
        return new Predicate<Version>() {
            @Override
            public boolean apply(Version input) {
                return publisher.equals(input.getProvider());
            }
        };
    }
}
