package org.atlasapi.remotesite.opta.events;

import org.atlasapi.remotesite.events.EventsFetcher;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;


public interface OptaEventsFetcher<T extends OptaTeam, M extends OptaMatch> extends EventsFetcher<OptaSportType, T, M> {

}
