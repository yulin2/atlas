package org.atlasapi.remotesite.opta.events;

import java.io.InputStream;

import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;


public interface OptaDataTransformer<T extends OptaTeam, M extends OptaMatch> {

    OptaEventsData<T, M> transform(InputStream input);
}
