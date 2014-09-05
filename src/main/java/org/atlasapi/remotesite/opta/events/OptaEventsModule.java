package org.atlasapi.remotesite.opta.events;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import joptsimple.internal.Strings;

import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.events.EventTopicResolver;
import org.atlasapi.remotesite.events.EventsUriCreator;
import org.atlasapi.remotesite.events.S3FileFetcher;
import org.atlasapi.remotesite.opta.events.model.OptaSportConfiguration;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.soccer.OptaSoccerDataHandler;
import org.atlasapi.remotesite.opta.events.soccer.OptaSoccerDataTransformer;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;
import org.atlasapi.remotesite.opta.events.sports.OptaSportsDataHandler;
import org.atlasapi.remotesite.opta.events.sports.OptaSportsDataTransformer;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixture;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsTeam;
import org.atlasapi.remotesite.util.RestS3ServiceSupplier;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.google.api.client.util.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.security.UsernameAndPassword;


public class OptaEventsModule {

    private static final String SCOTTISH_PREMIER_LEAGUE_FILENAME = "F1_14_2014.json";
    private static final String GERMAN_BUNDESLIGA_FILENAME = "F1_22_2014.json";
    private static final String RUGBY_LEAGUE_FILENAME = "RU1_201_2015.json";
    private static final String OPTA_HTTP_CONFIG_PREFIX = "opta.events.http.sports.";
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired EventStore eventStore;
    private @Autowired OrganisationStore organisationStore;
    private @Autowired EventTopicResolver topicResolver;
    
    private @Value("${s3.access}") String s3AccessKey;
    private @Value("${s3.secret}") String s3SecretAccessKey;
    private @Value("${opta.events.s3.bucket}") String s3BucketName;
    private @Value("${opta.events.http.baseUrl}") String baseUrl;
    private @Value("${opta.events.http.username}") String username;
    private @Value("${opta.events.http.password}") String password;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(soccerIngestTask().withName("Opta Soccer Events Updater"), RepetitionRules.NEVER);
        scheduler.schedule(sportsIngestTask().withName("Opta Sports Events Updater"), RepetitionRules.NEVER);
    }

    private OptaEventsIngestTask<SoccerTeam, SoccerMatchData> soccerIngestTask() {
        return new OptaEventsIngestTask<SoccerTeam, SoccerMatchData>(soccerFetcher(), soccerDataHandler());
    }

    private OptaEventsFetcher<SoccerTeam, SoccerMatchData> soccerFetcher() {
        return new CombiningOptaEventsFetcher<>(ImmutableList.<OptaEventsFetcher<SoccerTeam, SoccerMatchData>>of(
                new S3OptaEventsFetcher<>(s3FileFetcher(), soccerFileNames(), soccerTransformer(), s3BucketName),
                new HttpOptaEventsFetcher<>(
                        sportConfig(), 
                        HttpClients.webserviceClient(), 
                        soccerTransformer(), 
                        new UsernameAndPassword(username, password), baseUrl)
        ));
    }
    
    private S3FileFetcher s3FileFetcher() {
        AWSCredentials credentials = new AWSCredentials(s3AccessKey, s3SecretAccessKey);
        return new S3FileFetcher(new RestS3ServiceSupplier(credentials));
    }
    
    private OptaDataTransformer<SoccerTeam, SoccerMatchData> soccerTransformer() {
        return new OptaSoccerDataTransformer();
    }
    
    private EventsUriCreator uriCreator() {
        return new OptaEventsUriCreator();
    }

    private Map<OptaSportType, OptaSportConfiguration> sportConfig() {
        Builder<OptaSportType, OptaSportConfiguration> configMapping = 
                ImmutableMap.<OptaSportType, OptaSportConfiguration>builder();
        
        Iterable<Entry<String, Parameter>> matchingParams = 
                Configurer.getParamsWithKeyMatching(Predicates.containsPattern(OPTA_HTTP_CONFIG_PREFIX));
        for (Entry<String, Parameter> property : matchingParams) {
            String sportKey = property.getKey().substring(OPTA_HTTP_CONFIG_PREFIX.length());
            String sportConfig = property.getValue().get();
            
            if (!Strings.isNullOrEmpty(sportConfig)) {
                OptaSportType sport = OptaSportType.valueOf(sportKey.toUpperCase());
                OptaSportConfiguration config = parseConfig(sportConfig);
                configMapping.put(sport, config);
            } else {
                log.warn("Opta HTTP configuration for sport {} is missing.", sportKey);
            }
        }
        return configMapping.build();
    }

    /**
     * Parses a String parameter into a set of three parameters required for the Opta Sports 
     * competition API. The format is [prefix].[sportName]=feedType|competition|seasonId
     * @param sportConfig
     * @return
     */
    private OptaSportConfiguration parseConfig(String sportConfig) {
        Iterable<String> configItems = Splitter.on('|').split(sportConfig);
        return OptaSportConfiguration.builder()
                .withFeedType(Iterables.get(configItems, 0))
                .withCompetition(Iterables.get(configItems, 1))
                .withSeasonId(Iterables.get(configItems, 2))
                .build();
    }

    private Map<OptaSportType, String> soccerFileNames() {
        return ImmutableMap.of(
                OptaSportType.FOOTBALL_SCOTTISH_PREMIER_LEAGUE, SCOTTISH_PREMIER_LEAGUE_FILENAME,
                OptaSportType.FOOTBALL_GERMAN_BUNDESLIGA, GERMAN_BUNDESLIGA_FILENAME
        );
    }

    @Bean
    private OptaSoccerDataHandler soccerDataHandler() {
        return new OptaSoccerDataHandler(organisationStore, eventStore, topicResolver, new OptaEventsMapper(), uriCreator());
    }

    private OptaEventsIngestTask<OptaSportsTeam, OptaFixture> sportsIngestTask() {
        return new OptaEventsIngestTask<>(sportsFetcher(), sportsDataHandler());
    }

    private OptaEventsFetcher<OptaSportsTeam, OptaFixture> sportsFetcher() {
        return new S3OptaEventsFetcher<>(s3FileFetcher(), sportFileNames(), sportsTransformer(), s3BucketName);
    }
    
    private OptaDataTransformer<OptaSportsTeam, OptaFixture> sportsTransformer() {
        return new OptaSportsDataTransformer();
    }

    private Map<OptaSportType, String> sportFileNames() {
        return ImmutableMap.of(
                OptaSportType.RUGBY, RUGBY_LEAGUE_FILENAME
        );
    }

    private OptaSportsDataHandler sportsDataHandler() {
        return new OptaSportsDataHandler(organisationStore, eventStore, topicResolver, new OptaEventsMapper(), uriCreator());
    }
}
