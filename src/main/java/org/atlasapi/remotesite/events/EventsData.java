package org.atlasapi.remotesite.events;


public interface EventsData<T, M> {

    Iterable<T> teams();
    Iterable<M> matches();
}
