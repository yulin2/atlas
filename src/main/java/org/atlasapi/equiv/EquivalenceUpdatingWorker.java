package org.atlasapi.equiv;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.messaging.v3.EntityUpdatedMessage;
import org.atlasapi.messaging.worker.v3.AbstractWorker;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class EquivalenceUpdatingWorker extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ContentResolver contentResolver;
    private final LookupEntryStore entryStore;
    private final EquivalenceUpdater<Content> equivUpdater;
    private final Predicate<Content> filter;

    public EquivalenceUpdatingWorker(ContentResolver contentResolver,
            LookupEntryStore entryStore,
            EquivalenceUpdater<Content> equivUpdater, Predicate<Content> filter) {
        this.contentResolver = checkNotNull(contentResolver);
        this.entryStore = checkNotNull(entryStore);
        this.equivUpdater = checkNotNull(equivUpdater);
        this.filter = checkNotNull(filter);
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        String eid = message.getEntityId();
        Content content;
        try {
            content = resolveId(Long.valueOf(eid));
        } catch (NumberFormatException nfe) {
            content = resolveUri(eid);
        }
        if (content == null) {
            log.warn("Resolved null/not Content for {} {} {}", 
                new Object[]{message.getEntitySource(), message.getEntityType(), eid});
            return;
        }
        if (filter.apply(content)) {
            log.debug("Updating equivalence: {} {} {}", 
                new Object[]{message.getEntitySource(), message.getEntityType(), eid});
            equivUpdater.updateEquivalences(content);
        } else {
            log.trace("Skipping equiv update: {} {} {}", 
                new Object[]{message.getEntitySource(), message.getEntityType(), eid});
        }
    }

    private Content resolveId(Long id) {
        Iterable<LookupEntry> entries = entryStore.entriesForIds(ImmutableSet.of(id));
        LookupEntry entry = entries.iterator().next();
        return resolveUri(entry.uri());
    }

    private Content resolveUri(String uri) {
        ImmutableSet<String> contentUri = ImmutableSet.of(uri);
        ResolvedContent resolved = contentResolver.findByCanonicalUris(contentUri);
        Maybe<Identified> possibleContent = resolved.get(uri);
        return isContent(possibleContent) ? (Content) possibleContent.requireValue()
                                          : null;
    }

    private boolean isContent(Maybe<Identified> possibleContent) {
        return possibleContent.valueOrNull() instanceof Content;
    }
    
}
