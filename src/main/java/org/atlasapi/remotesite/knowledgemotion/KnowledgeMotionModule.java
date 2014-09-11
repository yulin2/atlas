package org.atlasapi.remotesite.knowledgemotion;

import javax.annotation.PostConstruct;

import org.atlasapi.googlespreadsheet.GoogleSpreadsheetModule;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
<<<<<<< HEAD
=======
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.topic.TopicCreatingTopicResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.knowledgemotion.topics.TopicGuesser;
import org.atlasapi.remotesite.knowledgemotion.topics.cache.KeyphraseTopicCache;
import org.atlasapi.remotesite.knowledgemotion.topics.spotlight.SpotlightKeywordsExtractor;
import org.atlasapi.remotesite.knowledgemotion.topics.spotlight.SpotlightResourceParser;
import org.atlasapi.spreadsheet.GoogleSpreadsheetModule;
>>>>>>> 1618239a60fecf39ceea579b6e9b5d857af9ca03
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class KnowledgeMotionModule {

    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired GoogleSpreadsheetModule spreadsheet;

    private @Autowired DatabasedMongo mongo;

    /**
     * Here we wire what is in fact a {@link TopicCreatingTopicResolver}, so we may create new topics where necessary.
     */
    @Qualifier("topicStore")
    @Autowired
    private TopicStore topicStore;

    @Autowired
    private TopicQueryResolver topicQueryResolver;

    static final ImmutableList<KnowledgeMotionSourceConfig> SOURCES = ImmutableList.of(
            KnowledgeMotionSourceConfig.from("GlobalImageworks", Publisher.KM_GLOBALIMAGEWORKS, "globalImageWorks:%s", "http://globalimageworks.com/%s"),
            KnowledgeMotionSourceConfig.from("Bloomberg", Publisher.KM_BLOOMBERG, "bloomberg:%s", "http://bloomberg.com/%s")
    );

    @PostConstruct
    public void startBackgroundTasks() {
<<<<<<< HEAD
        scheduler.schedule(globalImageUpdater().withName("KnowledgeMotion Spreadsheet Updater"), RepetitionRules.NEVER);
=======
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("KnowledgeMotion Spreadsheet updater"));
        scheduler.schedule(knowledgeMotionUpdater().withName("KnowledgeMotion Spreadsheet Updater"), RepetitionRules.NEVER);
    }

    private TopicGuesser topicGuesser() {
        return new TopicGuesser(
                new SpotlightKeywordsExtractor(new SpotlightResourceParser()),
                new KeyphraseTopicCache(mongo),
                topicStore
        );
>>>>>>> 1618239a60fecf39ceea579b6e9b5d857af9ca03
    }

    private KnowledgeMotionUpdateTask knowledgeMotionUpdater() {
        return new KnowledgeMotionUpdateTask(spreadsheet.spreadsheetFetcher(), 
                new DefaultKnowledgeMotionDataRowHandler(contentResolver, contentWriter, new KnowledgeMotionDataRowContentExtractor(SOURCES, topicGuesser()), topicQueryResolver),
                new KnowledgeMotionAdapter());
    }

}
