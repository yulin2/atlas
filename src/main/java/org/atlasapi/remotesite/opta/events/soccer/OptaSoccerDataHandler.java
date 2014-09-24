package org.atlasapi.remotesite.opta.events.soccer;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.util.List;

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
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerStats;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;


public final class OptaSoccerDataHandler extends OptaDataHandler<SoccerTeam, SoccerMatchData>{

    private static final String VENUE_TYPE = "Venue";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final OptaEventsMapper mapper;
    private final EventsUriCreator uriCreator;

    public OptaSoccerDataHandler(OrganisationStore organisationStore, EventStore eventStore, EventTopicResolver topicResolver, 
            OptaEventsMapper mapper, EventsUriCreator uriCreator) {
        super(organisationStore, eventStore, topicResolver, mapper);
        this.mapper = checkNotNull(mapper);
        this.uriCreator = checkNotNull(uriCreator);
    }
    
    @Override
    public Optional<Organisation> parseOrganisation(SoccerTeam team, OptaSportType sport) {
        Organisation organisation = new Organisation();

        if (mapper.fetchIgnoredTeams().contains(team.name())) {
            log.warn("Found team with ignored name {}", team.name());
            return Optional.absent();
        }
        organisation.setCanonicalUri(uriCreator.createTeamUri(team.attributes().uId()));
        organisation.setPublisher(Publisher.OPTA);
        organisation.setTitle(team.name());
        organisation.setEventGroups(resolveOrCreateEventGroups(sport));

        return Optional.of(organisation);
    }
    
    @Override
    public Optional<Event> parseEvent(SoccerMatchData match, OptaSportType sport) {
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

        event.setCanonicalUri(uriCreator.createEventUri(match.attributes().uId()));
        
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
        Optional<Organisation> team = getTeamByUri(uriCreator.createTeamUri(teamId));
        if (!team.isPresent()) {
            log.error("team {} not present in teams list", teamId);
            return Optional.absent();
        }
        return Optional.fromNullable(team.get().getTitle());
    }

    // we ignore the timezone string, as it uses the three letter codes such as 'BST', which are ambiguous 
    // (BST is either British Summer Time or Bangladesh Standard Time)
    private DateTime parseStartTime(SoccerMatchData match, OptaSportType sport) {
        String dateStr = match.matchInformation().date().date();
        DateTimeZone timeZone = mapper.fetchTimeZone(sport);
        return DATE_TIME_FORMATTER.withZone(timeZone)
                        .parseDateTime(dateStr);
    }

    @Override
    public String extractLocation(SoccerMatchData match) {
        return getVenueName(match.stats());
    }
    
    private String getVenueName(List<SoccerStats> stats) {
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
                return getTeamByUri(uriCreator.createTeamUri(input.attributes().teamRef())).orNull();
            }
        });
        return Iterables.filter(organisations, Predicates.notNull());
    }
}
