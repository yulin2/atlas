package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Deprecated
public class PublisherSwitchingContentEquivalenceUpdater implements EquivalenceUpdater<Content> {

    private Map<Publisher, EquivalenceUpdater<Content>> backingMap;

    public PublisherSwitchingContentEquivalenceUpdater(Map<Publisher, EquivalenceUpdater<Content>> publisherUpdaters) {
        this.backingMap = ImmutableMap.copyOf(publisherUpdaters);
    }
    
    public EquivalenceUpdater<Content> updaterFor(Publisher publisher) {
        return backingMap.get(publisher);
    }

    @Override
    public void updateEquivalences(Content content) {
        updaterFor(content.getPublisher()).updateEquivalences(content);
    }
}
