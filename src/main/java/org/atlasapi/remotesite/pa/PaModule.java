package org.atlasapi.remotesite.pa;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.s3.S3Client;
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
    private @Value("${s3.access}") String s3access;
    private @Value("${s3.secret}") String s3secret;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(paUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO)
            .withDescription("PA update scheduled task installed")
            .withSource(PaUpdater.class));
    } 
    
    public @Bean PaUpdater paUpdater() {
        S3Client s3client = new S3Client(s3access, s3secret, "pa-data");
        PaLocalFileManager fileManager = new PaLocalFileManager(ftpHost, ftpUsername, ftpPassword, ftpPath, localFilesPath, s3client, log);
        PaProgrammeProcessor processor = new PaProgrammeProcessor(contentWriter, contentResolver, log);
        return new PaUpdater(processor, fileManager, log);
    }
}
