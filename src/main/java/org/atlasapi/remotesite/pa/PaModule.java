package org.atlasapi.remotesite.pa;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;

@Configuration
public class PaModule {
private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired AdapterLog log;
    
    private @Value("${pa.ftp.username}") String ftpUsername;
    private @Value("${pa.ftp.password}") String ftpPassword;
    private @Value("${pa.ftp.host}") String ftpHost;
    private @Value("${pa.ftp.path}") String ftpPath;
    private @Value("${pa.filesPath}") String localFilesPath;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(paUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO)
            .withDescription("PA update scheduled task installed")
            .withSource(PaUpdater.class));
    } 
    
    public @Bean PaUpdater paUpdater() {
        return new PaUpdater(contentWriter, contentResolver, ftpHost, ftpUsername, ftpPassword, ftpPath, localFilesPath, log);
    }
}
