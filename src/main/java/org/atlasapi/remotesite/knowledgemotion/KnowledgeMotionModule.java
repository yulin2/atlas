package org.atlasapi.remotesite.knowledgemotion;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.spreadsheet.GoogleSpreadsheetModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class KnowledgeMotionModule {

    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired GoogleSpreadsheetModule spreadsheet;

    private static final ImmutableList<KnowledgeMotionSourceConfig> SOURCES = ImmutableList.of(
    );

    @PostConstruct
    public void startBackgroundTasks() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("KnowledgeMotion Spreadsheet updater"));
        scheduler.schedule(globalImageUpdater().withName("KnowledgeMotion Spreadsheet Updater"), RepetitionRules.NEVER);
    }

    private KnowledgeMotionUpdateTask globalImageUpdater() {
        return new KnowledgeMotionUpdateTask(spreadsheet.spreadsheetFetcher(), 
                new DefaultKnowledgeMotionDataRowHandler(contentResolver, contentWriter, new KnowledgeMotionDataRowContentExtractor(SOURCES)), 
                new KnowledgeMotionAdapter());
    }

}
