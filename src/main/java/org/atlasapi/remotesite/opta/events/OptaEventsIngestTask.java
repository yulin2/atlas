package org.atlasapi.remotesite.opta.events;

import org.atlasapi.remotesite.events.EventsIngestTask;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;
import org.slf4j.LoggerFactory;


public class OptaEventsIngestTask<T extends OptaTeam, M extends OptaMatch> extends EventsIngestTask<OptaSportType, T, M> {
    
    public OptaEventsIngestTask(OptaEventsFetcher<T, M> fetcher, OptaDataHandler<T, M> dataHandler) {
        super(LoggerFactory.getLogger(OptaEventsIngestTask.class), fetcher, dataHandler);
    }
}
