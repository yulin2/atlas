package org.atlasapi.remotesite.opta.events.sports;

import static com.google.api.client.util.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.remotesite.events.EventTopicResolver;
import org.atlasapi.remotesite.events.EventsUriCreator;
import org.atlasapi.remotesite.opta.events.OptaDataHandler;
import org.atlasapi.remotesite.opta.events.OptaEventsMapper;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixture;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixtureTeam;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsTeam;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;


public class OptaSportsDataHandler extends OptaDataHandler<OptaSportsTeam, OptaFixture> {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final OptaEventsMapper mapper;
    private final EventsUriCreator uriCreator;

    public OptaSportsDataHandler(OrganisationStore organisationStore, EventStore eventStore, EventTopicResolver topicResolver, 
            OptaEventsMapper mapper, EventsUriCreator uriCreator) {
        super(organisationStore, eventStore, topicResolver, mapper);
        this.mapper = checkNotNull(mapper);
        this.uriCreator = checkNotNull(uriCreator);
    }

    @Override
    public Optional<Organisation> parseOrganisation(OptaSportsTeam team) {
        Organisation organisation = new Organisation();

        if (mapper.fetchIgnoredTeams().contains(team.attributes().name())) {
            log.warn("Found team with ignored name {}", team.attributes().name());
            return Optional.absent();
        }
        organisation.setCanonicalUri(uriCreator.createTeamUri(team.attributes().id()));
        organisation.setPublisher(Publisher.OPTA);
        organisation.setTitle(team.attributes().name());

        return Optional.of(organisation);
    }

    @Override
    public Optional<Event> parseEvent(OptaFixture match, OptaSportType sport) {
        Optional<String> title = createTitle(match);
        if (!title.isPresent()) {
            return Optional.absent();
        }
        DateTime startTime = parseStartTime(match, sport);
        
        Optional<Topic> venue = fetchLocationTopic(match, sport);
        if (!venue.isPresent()) {
            return Optional.absent();
        }
        
        Duration duration = mapper.fetchDuration(sport);
        
        Event event = Event.builder()
                .withTitle(title.get())
                .withPublisher(Publisher.OPTA)
                .withVenue(venue.get())
                .withStartTime(startTime)
                .withEndTime(startTime.plus(duration))
                .withOrganisations(parseOrganisations(match))
                .withEventGroups(resolveOrCreateEventGroups(sport))
                .build();

        event.setCanonicalUri(uriCreator.createEventUri(match.attributes().id()));
        
        return Optional.of(event);
    }
    
    private Optional<String> createTitle(OptaFixture match) {
        Optional<String> team1 = fetchTeamName(match.teams().get(0));
        Optional<String> team2 = fetchTeamName(match.teams().get(1));
        if (!team1.isPresent() || !team2.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(team1.get() + " vs " + team2.get());
    }
    
    private Optional<String> fetchTeamName(OptaFixtureTeam teamData) {
        Optional<Organisation> team = getTeamByUri(uriCreator.createTeamUri(teamData.attributes().teamId()));
        if (!team.isPresent()) {
            log.error("team {} not present in teams list", teamData.attributes().teamId());
            return Optional.absent();
        }
        return Optional.of(team.get().getTitle());
    }
    
    private DateTime parseStartTime(OptaFixture fixture, OptaSportType sport) {
        DateTimeZone timeZone = mapper.fetchTimeZone(sport);
        return TIME_FORMATTER.withZone(timeZone)
                        .parseDateTime(fixture.attributes().gameDate() + " " + fixture.attributes().time());
    }
    
    private Iterable<Organisation> parseOrganisations(OptaFixture fixture) {
        Iterable<Organisation> organisations = Iterables.transform(fixture.teams(), new Function<OptaFixtureTeam, Organisation>() {
            @Override
            public Organisation apply(OptaFixtureTeam input) {
                return getTeamByUri(uriCreator.createTeamUri(input.attributes().teamId())).orNull();
            }
        });
        return Iterables.filter(organisations, Predicates.notNull());
    }

    @Override
    public String extractLocation(OptaFixture match) {
        return match.attributes().venue();
    }
}
