package org.atlasapi.remotesite.opta.events;

import org.atlasapi.remotesite.events.EventsData;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;


public interface OptaEventsData<T extends OptaTeam, M extends OptaMatch> extends EventsData<T, M> {

}
