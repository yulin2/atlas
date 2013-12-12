package org.atlasapi.equiv;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.messaging.v3.EntityUpdatedMessage;
import org.atlasapi.messaging.worker.v3.AbstractWorker;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class EquivalenceUpdatingWorker extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ContentResolver contentResolver;
    private final EquivalenceUpdater<Content> equivUpdater;
    private final Predicate<Content> filter;

    public EquivalenceUpdatingWorker(ContentResolver contentResolver,
            EquivalenceUpdater<Content> equivUpdater, Predicate<Content> filter) {
        this.contentResolver = checkNotNull(contentResolver);
        this.equivUpdater = checkNotNull(equivUpdater);
        this.filter = checkNotNull(filter);
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        String uri = message.getEntityId();
        Content content = resolve(uri);
        if (content == null) {
            log.warn("Resolved null/not Content for {}", uri);
            return;
        }
        if (filter.apply(content)) {
            log.debug("Updating equivalence: {}", uri);
            equivUpdater.updateEquivalences(content);
        } else {
            log.trace("Skipping equiv update: {}", uri);
        }
    }

    private Content resolve(String uri) {
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
