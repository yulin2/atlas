package org.atlasapi.remotesite.events;

import java.util.Set;

import com.google.common.base.Optional;


public interface EventsFetcher<S, T, M> {

    Optional<? extends EventsData<T, M>> fetch(S sport);
    
    Set<S> sports();
}
