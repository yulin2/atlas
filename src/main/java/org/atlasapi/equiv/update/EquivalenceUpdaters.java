package org.atlasapi.equiv.update;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class EquivalenceUpdaters implements EquivalenceUpdater<Content> {

    private Table<Publisher, Class<? extends Content>, EquivalenceUpdater<? extends Content>> updaters;

    public EquivalenceUpdaters() {
        this.updaters = HashBasedTable.create();
    }
    
    public <T extends Content> EquivalenceUpdaters register(Publisher publisher, Class<T> type, EquivalenceUpdater<T> updater) {
        updaters.put(publisher, type, updater);
        return this;
    }

    @Override
    public void updateEquivalences(Content subject) {
        update(subject);
    }

    private <T extends Content> void update(T subject) {
        Publisher publisher = subject.getPublisher();
        EquivalenceUpdater<T> updater = updaterFor(subject.getClass(), publisher);
        if (updater != null) {
            updater.updateEquivalences(subject);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Content> EquivalenceUpdater<T> updaterFor(Class<?> cls, Publisher publisher) {
        EquivalenceUpdater<T> updater = null;
        while(cls != null && updater == null) {
            updater = (EquivalenceUpdater<T>) updaters.get(publisher, cls);
            cls = cls.getSuperclass();
        }
        return updater;
    }
    
}
