package org.atlasapi.remotesite.opta.events.soccer;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.remotesite.opta.events.OptaDataHandler;
import org.atlasapi.remotesite.opta.events.OptaEventsUtility;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerStats;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;


public final class OptaSoccerDataHandler extends OptaDataHandler<SoccerTeam, SoccerMatchData>{

    private static final String VENUE_TYPE = "Venue";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final OptaEventsUtility utility;

    public OptaSoccerDataHandler(OrganisationStore organisationStore, EventStore eventStore, OptaEventsUtility utility) {
        super(organisationStore, eventStore);
        this.utility = checkNotNull(utility);
    }
    
    @Override
    public Optional<Organisation> parseOrganisation(SoccerTeam team) {
        Organisation organisation = new Organisation();

        organisation.setCanonicalUri(utility.createTeamUri(team.attributes().uId()));
        organisation.setPublisher(Publisher.OPTA);
        organisation.setTitle(team.name());

        return Optional.of(organisation);
    }
    
    @Override
    public Optional<Event> parseEvent(SoccerMatchData match, OptaSportType sport) {
        Optional<String> title = createTitle(match);
        if (!title.isPresent()) {
            return Optional.absent();
        }
        DateTime startTime = parseStartTime(match);
        
        Optional<Topic> venue = createOrResolveVenue(match);
        if (!venue.isPresent()) {
            return Optional.absent();
        }
        
        Optional<DateTime> endTime = utility.createEndTime(sport, startTime);
        if (!endTime.isPresent()) {
            log.error("No duration mapping found for sport {}", sport.name());
            return Optional.absent();
        }
        
        Event event = Event.builder()
                .withTitle(title.get())
                .withPublisher(Publisher.OPTA)
                .withVenue(venue.get())
                .withStartTime(startTime)
                .withEndTime(endTime.get())
                .withOrganisations(parseOrganisations(match))
                .withEventGroups(parseEventGroups(sport))
                .build();

        event.setCanonicalUri(utility.createEventUri(match.attributes().uId()));
        
        return Optional.of(event);
    }
    
    private Optional<String> createTitle(SoccerMatchData match) {
        Optional<String> team1 = fetchTeamName(match.teamData().get(0));
        Optional<String> team2 = fetchTeamName(match.teamData().get(1));
        if (!team1.isPresent() || !team2.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(team1.get() + " vs " + team2.get());
    }
    
    private Optional<String> fetchTeamName(SoccerTeamData teamData) {
        String teamId = teamData.attributes().teamRef();
        Optional<Organisation> team = getTeamByUri(utility.createTeamUri(teamId));
        if (!team.isPresent()) {
            log.error("team {} not present in teams list", teamId);
            return Optional.absent();
        }
        return Optional.fromNullable(team.get().getTitle());
    }

    
    private DateTime parseStartTime(SoccerMatchData match) {
        String dateStr = match.matchInformation().date().date();
        String timeZoneStr = match.matchInformation().timeZone();
        
        return DATE_TIME_FORMATTER.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZoneStr)))
                .parseDateTime(dateStr);
    }
    
    private Optional<Topic> createOrResolveVenue(SoccerMatchData match) {
        String location = getVenueData(match.stats());
        Optional<Topic> value = utility.createOrResolveVenue(location);
        if (!value.isPresent()) {
            log.error("Unable to resolve location: {}", location);
        }
        return value;
    }
    
    private String getVenueData(List<SoccerStats> stats) {
        return Iterables.getOnlyElement(Iterables.filter(stats, new Predicate<SoccerStats>() {
            @Override
            public boolean apply(SoccerStats input) {
                return VENUE_TYPE.equals(input.attributes().type());
            }
        })).value(); 
    }
    
    private Iterable<Organisation> parseOrganisations(SoccerMatchData match) {
        Iterable<Organisation> organisations = Iterables.transform(match.teamData(), new Function<SoccerTeamData, Organisation>() {
            @Override
            public Organisation apply(SoccerTeamData input) {
                return getTeamByUri(utility.createTeamUri(input.attributes().teamRef())).orNull();
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
