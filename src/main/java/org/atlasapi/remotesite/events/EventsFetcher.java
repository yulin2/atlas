package org.atlasapi.remotesite.events;

import java.util.Set;

import com.google.common.base.Optional;


public interface EventsFetcher<S, T, M> {

    /**
     * For a given sport, fetches relevant data and returns it. If there is an error in
     * fetching data for a valid sport, then Optional.absent is returned.
     * @param sport the sport to fetch data for
     * @throws IllegalArgumentException if the sport provided is not supported by the fetcher
     */
    Optional<? extends EventsData<T, M>> fetch(S sport);
    
    Set<S> sports();
}
