package org.atlasapi.remotesite.opta.events;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;

import org.atlasapi.remotesite.events.EventsData;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;


public final class CombiningOptaEventsFetcher<T extends OptaTeam, M extends OptaMatch> implements OptaEventsFetcher<T, M> {
    
    private final Map<OptaSportType, OptaEventsFetcher<T, M>> fetcherMap;

    public CombiningOptaEventsFetcher(Iterable<OptaEventsFetcher<T, M>> fetchers) {
        this.fetcherMap = createFetcherMap(checkNotNull(fetchers));
    }

    private Map<OptaSportType, OptaEventsFetcher<T, M>> createFetcherMap(
            Iterable<OptaEventsFetcher<T, M>> fetchers) {
        ImmutableMap.Builder<OptaSportType, OptaEventsFetcher<T, M>> mapping = ImmutableMap.<OptaSportType, OptaEventsFetcher<T, M>>builder();
        for (OptaEventsFetcher<T, M> fetcher : fetchers) {
            for (OptaSportType sport : fetcher.sports()) {
                mapping.put(sport, fetcher);
            }
        }
        return mapping.build();
    }

    @Override
    public Optional<? extends EventsData<T, M>> fetch(OptaSportType sport) {
        OptaEventsFetcher<T, M> fetcher = fetcherMap.get(sport);
        if (fetcher == null) {
            return Optional.absent();
        }
        return fetcher.fetch(sport);
    }

    @Override
    public Set<OptaSportType> sports() {
        return fetcherMap.keySet();
    }

}
