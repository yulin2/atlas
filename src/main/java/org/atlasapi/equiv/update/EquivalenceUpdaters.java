package org.atlasapi.equiv.update;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.Maps;

public class EquivalenceUpdaters implements EquivalenceUpdater<Content> {

    private Map<Publisher, EquivalenceUpdater<Content>> updaters;

    public EquivalenceUpdaters() {
        this.updaters = Maps.newHashMap();
    }
    
    public EquivalenceUpdaters register(Publisher publisher, EquivalenceUpdater<Content> updater) {
        updaters.put(publisher, updater);
        return this;
    }

    @Override
    public void updateEquivalences(Content subject) {
        updaters.get(subject.getPublisher()).updateEquivalences(subject);
    }

}
