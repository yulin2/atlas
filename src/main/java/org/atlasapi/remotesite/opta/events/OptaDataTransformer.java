package org.atlasapi.remotesite.opta.events;

import org.atlasapi.remotesite.events.EventsDataTransformer;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;


public interface OptaDataTransformer<T extends OptaTeam, M extends OptaMatch> extends EventsDataTransformer<T, M> {

}
