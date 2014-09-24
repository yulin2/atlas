package org.atlasapi.remotesite.events;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;


public abstract class EventParsingDataHandler<S, T, M> implements DataHandler<S, T, M> {

    private final Map<String, Organisation> teamNameMapping = Maps.newHashMap();
    
    private final OrganisationStore organisationStore;
    private final EventStore eventStore;
    private final EventTopicResolver topicResolver;
    private final EventsFieldMapper<S> mapper;

    public EventParsingDataHandler(OrganisationStore organisationStore, EventStore eventStore, 
            EventTopicResolver topicResolver, EventsFieldMapper<S> mapper) {
        this.organisationStore = checkNotNull(organisationStore);
        this.eventStore = checkNotNull(eventStore);
        this.topicResolver = checkNotNull(topicResolver);
        this.mapper = checkNotNull(mapper);
    }
    
    @Override
    public void handleTeam(T team, S sport) {
        Optional<Organisation> organisation = parseOrganisation(team, sport);
        if (organisation.isPresent()) {
            createOrMerge(organisation.get());
        }
    }

    @Override
    public void handleMatch(M match, S sport) {
        Optional<Event> event = parseEvent(match, sport);
        if (event.isPresent()) {
            createOrMerge(event.get());
        }
    }

    public abstract Optional<Organisation> parseOrganisation(T team, S sport);
    
    public abstract Optional<Event> parseEvent(M match, S sport);

    public abstract String extractLocation(M match);
    
    private void createOrMerge(Organisation newOrganisation) {
        Optional<Organisation> resolved = organisationStore.organisation(newOrganisation.getCanonicalUri());
        if (resolved.isPresent()) {
            Organisation existing = resolved.get();

            existing.setTitle(newOrganisation.getTitle());
            existing.setMembers(newOrganisation.members());

            writeOrganisation(existing);
        } else {
            writeOrganisation(newOrganisation);
        }
    }
    
    public Optional<Topic> fetchLocationTopic(M match, S sport) {
        String location = extractLocation(match);
        if (mapper.fetchIgnoredLocations(sport).contains(location)) {
            return Optional.absent();
        }
        Optional<String> locationUrl = mapper.fetchLocationUrl(location);
        if (!locationUrl.isPresent()) {
            throw new NoSuchMappingException("No mapping for location " + location);
        }
        return Optional.of(topicResolver.createOrResolveVenue(location, locationUrl.get()));
    }

    public Optional<Organisation> getTeamByUri(String uri) {
        return Optional.fromNullable(teamNameMapping.get(uri));
    }
    
    public Iterable<Topic> resolveOrCreateEventGroups(S sport) {
        Map<String, String> eventGroupTopicUrls = mapper.fetchEventGroupUrls(sport);
        return topicResolver.createOrResolveEventGroups(eventGroupTopicUrls);
    }

    /**
     * Writes an {@link Organisation}, and adds to an in-class cache of uri -> Organisation
     * @param organisation
     */
    private void writeOrganisation(Organisation organisation) {
        organisationStore.createOrUpdateOrganisation(organisation);
        teamNameMapping.put(organisation.getCanonicalUri(), organisation);
    }
    
    private void createOrMerge(Event newEvent) {
        Optional<Event> resolved = eventStore.fetch(newEvent.getCanonicalUri());
        if (resolved.isPresent()) {
            Event existing = resolved.get();
            
            existing.setTitle(newEvent.title());
            existing.setVenue(newEvent.venue());
            existing.setStartTime(newEvent.startTime());
            existing.setEndTime(newEvent.endTime());
            existing.setParticipants(newEvent.participants());
            existing.setOrganisations(newEvent.organisations());
            existing.setEventGroups(newEvent.eventGroups());
            
            eventStore.createOrUpdate(existing);
        } else {
            eventStore.createOrUpdate(newEvent);
        }
    }
}
