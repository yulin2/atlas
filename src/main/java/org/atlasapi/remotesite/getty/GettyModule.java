package org.atlasapi.remotesite.getty;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.security.UsernameAndPassword;

@Configuration
public class GettyModule {

    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    
    @Value("${getty.pagination}") private String gettyPagination;
    @Value("${iris.user}") private String irisUser;
    @Value("${iris.pswd}") private String irisPswd;
    
    @PostConstruct
    public void startBackgroundTasks() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Getty updater"));
        scheduler.schedule(gettyUpdater().withName("Getty Updater"), RepetitionRules.NEVER);
    }
    
    private GettyUpdateTask gettyUpdater() {
        Integer itemsPerPage = Integer.valueOf(gettyPagination);
        return new GettyUpdateTask(new GettyAdapter(), 
                new DefaultGettyDataHandler(contentResolver, contentWriter, new GettyContentExtractor()), 
                new GettyTokenFetcher(), 
                new GettyVideoFetcher(new JsonVideoRequest(), itemsPerPage),
                new IrisKeywordsFetcher(new UsernameAndPassword(irisUser, irisPswd)),
                itemsPerPage);
    }
    
}
