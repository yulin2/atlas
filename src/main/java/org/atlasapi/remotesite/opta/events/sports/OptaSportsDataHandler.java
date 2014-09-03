package org.atlasapi.remotesite.opta.events.sports;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.remotesite.opta.events.OptaDataHandler;
import org.atlasapi.remotesite.opta.events.OptaEventsUtility;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixture;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixtureTeam;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsTeam;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;


public class OptaSportsDataHandler extends OptaDataHandler<OptaSportsTeam, OptaFixture> {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final OptaEventsUtility utility;

    public OptaSportsDataHandler(OrganisationStore organisationStore, EventStore eventStore, OptaEventsUtility utility) {
        super(organisationStore, eventStore);
        this.utility = checkNotNull(utility);
    }

    @Override
    public Optional<Organisation> parseOrganisation(OptaSportsTeam team) {
        Organisation organisation = new Organisation();

        organisation.setCanonicalUri(utility.createTeamUri(team.attributes().id()));
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
        Optional<DateTime> startTime = parseStartTime(match, sport);
        if (!startTime.isPresent()) {
            return Optional.absent();
        }
        
        Optional<Topic> venue = createOrResolveVenue(match);
        if (!venue.isPresent()) {
            return Optional.absent();
        }
        
        Optional<DateTime> endTime = utility.createEndTime(sport, startTime.get());
        if (!endTime.isPresent()) {
            log.error("No duration mapping exists for sport {}", sport.name());
            return Optional.absent();
        }
        Event event = Event.builder()
                .withTitle(title.get())
                .withPublisher(Publisher.OPTA)
                .withVenue(venue.get())
                .withStartTime(startTime.get())
                .withEndTime(endTime.get())
                .withOrganisations(parseOrganisations(match))
                .withEventGroups(parseEventGroups(sport))
                .build();

        event.setCanonicalUri(utility.createEventUri(match.attributes().id()));
        
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
        Optional<Organisation> team = getTeamByUri(utility.createTeamUri(teamData.attributes().teamId()));
        if (!team.isPresent()) {
            log.error("team {} not present in teams list", teamData.attributes().teamId());
            return Optional.absent();
        }
        return Optional.of(team.get().getTitle());
    }
    
    private Optional<DateTime> parseStartTime(OptaFixture fixture, OptaSportType sport) {
        Optional<DateTimeZone> timeZone = utility.fetchTimeZone(sport);
        if (!timeZone.isPresent()) {
            log.error("No timezone mapping exists for sport {}", sport);
            return Optional.absent();
        }
        return Optional.of(
                TIME_FORMATTER.withZone(timeZone.get())
                        .parseDateTime(fixture.attributes().gameDate() + " " + fixture.attributes().time())
        );
    }
    
    private Optional<Topic> createOrResolveVenue(OptaFixture match) {
        String location = match.attributes().venue();
        Optional<Topic> value = utility.createOrResolveVenue(location);
        if (!value.isPresent()) {
            log.error("Unable to resolve location: {}", location);
        }
        return value;
    }
    
    private Iterable<Organisation> parseOrganisations(OptaFixture fixture) {
        Iterable<Organisation> organisations = Iterables.transform(fixture.teams(), new Function<OptaFixtureTeam, Organisation>() {
            @Override
            public Organisation apply(OptaFixtureTeam input) {
                return getTeamByUri(utility.createTeamUri(input.attributes().teamId())).orNull();
            }
        });
        return Iterables.filter(organisations, Predicates.notNull());
    }

    private Iterable<Topic> parseEventGroups(OptaSportType sport) {
        Optional<Set<Topic>> eventGroups = utility.parseEventGroups(sport);
        if (!eventGroups.isPresent()) {
            log.warn("No event groups mapped to sport {}", sport.name());
            return ImmutableList.of();
        } else {
            return eventGroups.get();
        }
    }
}
