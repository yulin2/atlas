package org.atlasapi.remotesite.opta.events;

import java.util.Map;
import java.util.Set;

import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.events.EventsUtility;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


public class OptaEventsUtility extends EventsUtility<OptaSportType> {
    
    private static final String EVENT_URI_BASE = "http://optasports.com/events/";
    private static final String TEAM_URI_BASE = "http://optasports.com/teams/";
    private static final Map<OptaSportType, Duration> DURATION_MAPPING = ImmutableMap.of(
            OptaSportType.RUGBY, Duration.standardMinutes(100),
            OptaSportType.FOOTBALL_GERMAN_BUNDESLIGA, Duration.standardMinutes(110),
            OptaSportType.FOOTBALL_SCOTTISH_PREMIER_LEAGUE, Duration.standardMinutes(110)
    );
    private static final Map<String, String> VENUE_LOOKUP = ImmutableMap.<String, String>builder()
            .put("Recreation Ground", "http://dbpedia.org/resources/Recreation_Ground_(Bath)")
            .put("Adams Park", "http://dbpedia.org/resources/Adams_Park")
            .put("stadium:mk", "http://dbpedia.org/resources/Stadium:mk")
            .put("The Stoop", "http://dbpedia.org/resources/The_Stoop")
            .put("Wembley Stadium", "http://dbpedia.org/resources/Wembley_stadium")
            .put("Welford Road", "http://dbpedia.org/resources/Welford_Road_Stadium")
            .put("Twickenham", "http://dbpedia.org/resources/Twickenham_Stadium")
            .put("Kassam Stadium", "http://dbpedia.org/resources/Kassam_Stadium")
            .put("Kingston Park", "http://dbpedia.org/resources/Kingston_Park_(stadium)")
            .put("Allianz Park", "http://dbpedia.org/resources/Barnet_Copthall")
            .put("Kingsholm", "http://dbpedia.org/resources/Kingsholm_Stadium")
            .put("Madejski Stadium", "http://dbpedia.org/resources/Madejski_Stadium")
            .put("AJ Bell Stadium", "http://dbpedia.org/resources/Salford_City_Stadium")
            .put("Sandy Park", "http://dbpedia.org/resources/Sandy_Park")
            .put("Franklin's Gardens", "http://dbpedia.org/resources/Franklin%27s_Gardens")
            .put("Allianz Arena", "http://dbpedia.org/resources/Allianz_Arena")
            .put("BayArena", "http://dbpedia.org/resources/BayArena")
            .put("Benteler-Arena", "http://dbpedia.org/resources/Benteler_Arena")
            .put("Borussia-Park", "http://dbpedia.org/resources/Borussia-Park")
            .put("Celtic Park", "http://dbpedia.org/resources/Celtic_Park")
            .put("Coface Arena", "http://dbpedia.org/resources/Coface_Arena")
            .put("Commerzbank Arena", "http://dbpedia.org/resources/Commerzbank-Arena")
            .put("Dens Park", "http://dbpedia.org/resources/Dens_Park")
            .put("Fir Park", "http://dbpedia.org/resources/Fir_Park")
            .put("Firhill Stadium", "http://dbpedia.org/resources/Firhill_Stadium")
            .put("HDI-Arena", "http://dbpedia.org/resources/HDI-Arena")
            .put("Imtech Arena", "http://dbpedia.org/resources/Volksparkstadion")
            .put("MAGE SOLAR Stadion", "http://dbpedia.org/resources/Mage_Solar_Stadion")
            .put("McDiarmid Park", "http://dbpedia.org/resources/McDiarmid_Park")
            .put("Mercedes-Benz Arena", "http://dbpedia.org/resources/Mercedes-Benz_Arena_(Stuttgart)")
            .put("New Douglas Park", "http://dbpedia.org/resources/New_Douglas_Park")
            .put("Olympiastadion", "http://dbpedia.org/resources/Olympic_Stadium_(Berlin)")
            .put("Pittodrie", "http://dbpedia.org/resources/Pittodrie_Stadium")
            .put("RheinEnergieStadion", "http://dbpedia.org/resources/RheinEnergieStadion")
            .put("Rugby Park", "http://dbpedia.org/resources/Rugby_Park")
            .put("SGL Arena", "http://dbpedia.org/resources/SGL_arena")
            .put("Signal Iduna Park", "http://dbpedia.org/resources/Signal_Iduna_Park")
            .put("St Mirren Park", "http://dbpedia.org/resources/St._Mirren_Park")
            .put("Tannadice Park", "http://dbpedia.org/resources/Tannadice_Park")
            .put("Tulloch Caledonian Stadium", "http://dbpedia.org/resources/Caledonian_Stadium")
            .put("VELTINS-Arena", "http://dbpedia.org/resources/Veltins-Arena")
            .put("Victoria Park, Dingwall", "http://dbpedia.org/resources/Victoria_Park,_Dingwall")
            .put("Volkswagen Arena", "http://dbpedia.org/resources/Volkswagen_Arena")
            .put("WIRSOL Rhein-Neckar-Arena", "http://dbpedia.org/resources/Rhein-Neckar_Arena")
            .put("Weserstadion", "http://dbpedia.org/resources/Weserstadion")
            .build();
    private static final Map<OptaSportType, Set<String>> EVENT_GROUPS_LOOKUP = ImmutableMap.<OptaSportType, Set<String>>builder()
            .put(OptaSportType.RUGBY, ImmutableSet.of(
                    "http://dbpedia.org/resources/Rugby_league", 
                    "http://dbpedia.org/resources/Rugby_football"
            ))
            .put(OptaSportType.FOOTBALL_SCOTTISH_PREMIER_LEAGUE, ImmutableSet.of(
                    "http://dbpedia.org/resources/Football", 
                    "http://dbpedia.org/resources/Association_football", 
                    "http://dbpedia.org/resources/Scottish_Premier_League"
            ))
            .put(OptaSportType.FOOTBALL_GERMAN_BUNDESLIGA, ImmutableSet.of(
                    "http://dbpedia.org/resources/Football", 
                    "http://dbpedia.org/resources/Association_football", 
                    "http://dbpedia.org/resources/German_Bundesliga"
            ))
            .build();
    
    public OptaEventsUtility(TopicStore topicStore) {
        super(topicStore);
    }

    @Override
    public String createEventUri(String id) {
        return EVENT_URI_BASE + id;
    }

    @Override
    public String createTeamUri(String id) {
        return TEAM_URI_BASE + id;
    }

    @Override
    public Optional<DateTime> createEndTime(OptaSportType sport, DateTime start) {
        Optional<Duration> duration = Optional.fromNullable(DURATION_MAPPING.get(sport));
        if (!duration.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(start.plus(duration.get()));
    }

    @Override
    public Optional<String> fetchLocationUrl(String location) {
        return Optional.fromNullable(VENUE_LOOKUP.get(location));
    }

    @Override
    public Optional<Set<String>> fetchEventGroupUrls(OptaSportType sport) {
        return Optional.fromNullable(EVENT_GROUPS_LOOKUP.get(sport));
    }

}
