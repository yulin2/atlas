package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class PublisherSwitchingContentEquivalenceUpdater implements EquivalenceUpdater<Content> {

    private Map<Publisher, EquivalenceUpdater<Content>> backingMap;

    public PublisherSwitchingContentEquivalenceUpdater(Map<Publisher, EquivalenceUpdater<Content>> publisherUpdaters) {
        this.backingMap = ImmutableMap.copyOf(publisherUpdaters);
    }
    
    public EquivalenceUpdater<Content> updaterFor(Publisher publisher) {
        return backingMap.get(publisher);
    }

    @Override
    public EquivalenceResult<Content> updateEquivalences(Content content, Optional<List<Content>> externalCandidates) {
        return updaterFor(content.getPublisher()).updateEquivalences(content, externalCandidates);
    }
}
