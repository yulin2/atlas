package org.atlasapi.remotesite.opta.events;

import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.remotesite.events.EventParsingDataHandler;
import org.atlasapi.remotesite.events.EventTopicResolver;
import org.atlasapi.remotesite.events.EventsFieldMapper;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;


public abstract class OptaDataHandler<T extends OptaTeam, M extends OptaMatch> extends EventParsingDataHandler<OptaSportType, T, M> {

    public OptaDataHandler(OrganisationStore organisationStore, EventStore eventStore, EventTopicResolver topicResolver, 
            EventsFieldMapper<OptaSportType> mapper) {
        super(organisationStore, eventStore, topicResolver, mapper);
    }

}
