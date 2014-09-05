package org.atlasapi.remotesite.opta.events.soccer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.opta.events.OptaEventsUtility;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.soccer.model.MatchDateDeserializer;
import org.atlasapi.remotesite.opta.events.soccer.model.OptaSoccerEventsFeed;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchInfo.MatchDate;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamDataDeserializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.metabroadcast.common.base.Maybe;


public class OptaSoccerDataHandlerTest {

    private static final OptaSportType SPORT = OptaSportType.FOOTBALL_GERMAN_BUNDESLIGA;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(MatchDate.class, new MatchDateDeserializer())
            .registerTypeAdapter(SoccerTeamData.class, new SoccerTeamDataDeserializer())
            .create();
    private OrganisationStore organisationStore = Mockito.mock(OrganisationStore.class);
    private EventStore eventStore = Mockito.mock(EventStore.class);
    private TopicStore topicStore = Mockito.mock(TopicStore.class);
    private OptaEventsUtility utility = new OptaEventsUtility(topicStore);
    private final OptaSoccerDataHandler handler = new OptaSoccerDataHandler(organisationStore, eventStore, utility);
    private OptaSoccerEventsData feedData;
    
    public OptaSoccerDataHandlerTest() throws JsonSyntaxException, JsonIOException, IOException {
        this.feedData = readDataFromFile("bundesliga_feed.json");
    }
    
    @Before
    public void setup() {
        Mockito.when(topicStore.topicFor(Mockito.matches("dbpedia"), Mockito.anyString())).then(new Answer<Maybe<Topic>>() {
            @Override
            public Maybe<Topic> answer(InvocationOnMock invocation) throws Throwable {
                Topic topic = new Topic(1234l);
                topic.setNamespace("dbpedia");
                topic.setValue((String) invocation.getArguments()[1]);
                return Maybe.just(topic);
            }
        });
        Mockito.when(organisationStore.organisation(Mockito.anyString())).thenReturn(Optional.<Organisation>absent());
    }
    
    
    @Test
    public void testTeamParsing() {
        SoccerTeam team = Iterables.getFirst(feedData.teams(), null);
        Optional<Organisation> parsed = handler.parseOrganisation(team);
        
        Organisation parsedTeam = parsed.get();
        
        assertEquals("http://optasports.com/teams/" + team.attributes().uId(), parsedTeam.getCanonicalUri());
        assertEquals(Publisher.OPTA, parsedTeam.getPublisher());
        assertEquals(team.name(), parsedTeam.getTitle());
    }
    
    @Test
    public void testEventParsing() {
        // This is necessary in order to populate the map of team uri -> Team held within the handler
        parseTeams();
        
        SoccerMatchData match = Iterables.getFirst(feedData.matches(), null);
        Optional<Event> parsed = handler.parseEvent(match, SPORT);
        
        Event parsedEvent = parsed.get();

        DateTime startTime = new DateTime(2014, 9, 27, 14, 30, 0, DateTimeZone.forID("Europe/Berlin"));
        ImmutableSet<String> expectedTeamUris = ImmutableSet.of("http://optasports.com/teams/t387", "http://optasports.com/teams/t156");
        
        assertEquals("http://optasports.com/events/" + match.attributes().uId(), parsedEvent.getCanonicalUri());
        assertEquals("1. FC Köln vs FC Bayern München", parsedEvent.title());
        assertEquals(Publisher.OPTA, parsedEvent.publisher());
        assertEquals(utility.fetchLocationUrl("RheinEnergieStadion").get(), parsedEvent.venue().getValue());
        assertEquals(startTime, parsedEvent.startTime());
        assertEquals(utility.createEndTime(SPORT, startTime).get(), parsedEvent.endTime());
        assertEquals(expectedTeamUris, transformToUris(parsedEvent.organisations()));
        assertTrue(parsedEvent.participants().isEmpty());
        assertEquals(transformToValues(utility.parseEventGroups(SPORT).get()), transformToValues(parsedEvent.eventGroups()));
        assertTrue(parsedEvent.content().isEmpty());
    }

    private void parseTeams() {
        for (SoccerTeam team : feedData.teams()) {
            handler.handle(team);
        }
    }

    private Set<String> transformToUris(List<Organisation> organisations) {
        return ImmutableSet.copyOf(Iterables.transform(organisations, new Function<Organisation, String>() {
            @Override
            public String apply(Organisation input) {
                return input.getCanonicalUri();
            }}));
    }

    private Set<String> transformToValues(Iterable<Topic> topics) {
        return ImmutableSet.copyOf(Iterables.transform(topics, new Function<Topic, String>() {
            @Override
            public String apply(Topic input) {
                return input.getValue();
            }
        }));
    }

    private OptaSoccerEventsData readDataFromFile(String filename) throws JsonSyntaxException, JsonIOException, IOException {
        URL testFile = Resources.getResource(getClass(), filename);
        OptaSoccerEventsFeed eventsFeed = gson.fromJson(new InputStreamReader(testFile.openStream()), OptaSoccerEventsFeed.class);
        return new OptaSoccerEventsData(eventsFeed.feed().document().matchData(), eventsFeed.feed().document().teams());
    }

}
