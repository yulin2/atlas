package org.atlasapi.equiv.update;

import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;

public class PublisherSwitchingContentEquivalenceUpdater implements ContentEquivalenceUpdater<Content> {

    private Map<Publisher, ContentEquivalenceUpdater<Content>> backingMap;

    public PublisherSwitchingContentEquivalenceUpdater(Map<Publisher, ContentEquivalenceUpdater<Content>> publisherUpdaters) {
        this.backingMap = ImmutableMap.copyOf(publisherUpdaters);
    }
    
    public ContentEquivalenceUpdater<Content> updaterFor(Publisher publisher) {
        return backingMap.get(publisher);
    }

    @Override
    public EquivalenceResult<Content> updateEquivalences(Content content) {
        return updaterFor(content.getPublisher()).updateEquivalences(content);
    }
}
