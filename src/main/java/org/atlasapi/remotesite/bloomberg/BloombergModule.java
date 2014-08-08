package org.atlasapi.remotesite.bloomberg;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.spreadsheet.GoogleSpreadsheetModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BloombergModule {

    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired GoogleSpreadsheetModule spreadsheet;
    
    @PostConstruct
    public void startBackgroundTasks() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Bloomberg updater"));
        scheduler.schedule(bloombergUpdater().withName("Bloomberg Spreadsheet Updater"), RepetitionRules.NEVER);
    }
    
    private BloombergUpdateTask bloombergUpdater() {
        return new BloombergUpdateTask(spreadsheet.spreadsheetFetcher(), 
                new DefaultBloombergDataRowHandler(contentResolver, contentWriter, new BloombergDataRowContentExtractor()), 
                new BloombergAdapter());
    }
    
}
