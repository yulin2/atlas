package org.atlasapi.remotesite.bt.events;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.organisation.OrganisationStore;
import org.atlasapi.persistence.event.EventStore;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.bt.events.model.BtSportType;
import org.atlasapi.remotesite.events.EventTopicResolver;
import org.atlasapi.remotesite.events.S3FileFetcher;
import org.atlasapi.remotesite.util.RestS3ServiceSupplier;
import org.jets3t.service.security.AWSCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BtEventsModule {
    
    private static final String UFC_FILENAME = "ufc_event_feed.json";
    private static final String MOTOGP_FILENAME = "motogp_event_feed.json";
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired EventStore eventStore;
    private @Autowired OrganisationStore organisationStore;
    private @Autowired @Qualifier("topicStore") TopicStore topicStore;
    
    private @Value("${s3.access}") String s3AccessKey;
    private @Value("${s3.secret}") String s3SecretAccessKey;
    private @Value("${bt.events.s3.bucket}") String s3BucketName;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(ingestTask().withName("BT Events Updater"), RepetitionRules.NEVER);
    }
    
    @Bean
    public BtEventsIngestTask ingestTask() {
        return new BtEventsIngestTask(fetcher(), dataHandler());
    }
    
    private BtEventsDataHandler dataHandler() {
        return new BtEventsDataHandler(organisationStore, eventStore, topicResolver(), new BtEventsFieldMapper(), new BtEventsUriCreator());
    }
    
    @Bean
    public EventTopicResolver topicResolver() {
        return new EventTopicResolver(topicStore);
    }

    private BtEventsFetcher fetcher() {
        return new S3BtEventsFetcher(fileFetcher(), fileNames(), s3BucketName, new BtEventsDataTransformer());
    }

    private S3FileFetcher fileFetcher() {
        AWSCredentials credentials = new AWSCredentials(s3AccessKey, s3SecretAccessKey);
        return new S3FileFetcher(new RestS3ServiceSupplier(credentials));
    }

    private Map<BtSportType, String> fileNames() {
        return ImmutableMap.of(
                BtSportType.MOTO_GP, MOTOGP_FILENAME,
                BtSportType.UFC, UFC_FILENAME
        );
    }
}
