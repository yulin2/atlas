package org.atlasapi.remotesite.voila;

import static org.atlasapi.remotesite.HttpClients.webserviceClient;

import java.text.ParseException;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class VoilaModule {

    private static final String TWITTER_NS_FOR_PROGRAMMES = "twitter";
    private static final String TWITTER_NS_FOR_AUDIENCE = "twitter:audience-related";
    private static final String CONTENT_WORDS_FOR_PROGRAMMES = "contentWords";
    private static final String CONTENT_WORDS_LIST_FOR_PROGRAMMES = "contentWordsList";
    private static final String CONTENT_WORDS_FOR_AUDIENCE = "contentWordsForPeopleTalk";
    private static final String CONTENT_WORDS_LIST_FOR_AUDIENCE = "contentWordsListForPeopleTalk";
    //
    private @Value("${cannon.host.name}")
    String cannonHostName;
    private @Value("${cannon.host.port}")
    Integer cannonHostPort;
    private @Autowired
    ContentResolver contentResolver;
    private @Autowired
    ContentWriter contentWriter;
    private @Autowired
    TopicStore topicStore;
    private @Autowired
    TopicQueryResolver topicResolver;
    private @Autowired
    SimpleScheduler scheduler;
    private @Autowired
    AdapterLog log;

    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(cannonTwitterTopicsUpdaterForProgrammes().withName("Twitter Topics Updater for Programmes"), RepetitionRules.NEVER);
        scheduler.schedule(cannonTwitterTopicsUpdaterForAudience().withName("Twitter Topics Updater for Audience-related Content"), RepetitionRules.NEVER);
    }

    @Bean
    CannonTwitterTopicsUpdater cannonTwitterTopicsUpdaterForProgrammes() {
        return new CannonTwitterTopicsUpdater(cannonTopicsClientForProgrammes(), contentTopicsUpdaterForProgrammes());
    }

    @Bean
    ContentTwitterTopicsUpdater contentTopicsUpdaterForProgrammes() {
        return new ContentTwitterTopicsUpdater(cannonTopicsClientForProgrammes(), contentResolver, TWITTER_NS_FOR_PROGRAMMES, topicStore, topicResolver, contentWriter, log);
    }

    @Bean
    CannonTwitterTopicsClient cannonTopicsClientForProgrammes() {
        try {
            return new CannonTwitterTopicsClient(webserviceClient(), HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), CONTENT_WORDS_LIST_FOR_PROGRAMMES, CONTENT_WORDS_FOR_PROGRAMMES, log);
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }
    }

    @Bean
    CannonTwitterTopicsUpdater cannonTwitterTopicsUpdaterForAudience() {
        return new CannonTwitterTopicsUpdater(cannonTopicsClientForAudience(), contentTopicsUpdaterForAudience());
    }

    @Bean
    ContentTwitterTopicsUpdater contentTopicsUpdaterForAudience() {
        return new ContentTwitterTopicsUpdater(cannonTopicsClientForAudience(), contentResolver, TWITTER_NS_FOR_AUDIENCE, topicStore, topicResolver, contentWriter, log);
    }

    @Bean
    CannonTwitterTopicsClient cannonTopicsClientForAudience() {
        try {
            return new CannonTwitterTopicsClient(webserviceClient(), HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), CONTENT_WORDS_LIST_FOR_AUDIENCE, CONTENT_WORDS_FOR_AUDIENCE, log);
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }
    }
}
