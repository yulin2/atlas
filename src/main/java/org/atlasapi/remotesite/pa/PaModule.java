package org.atlasapi.remotesite.pa;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.atlasapi.s3.DefaultS3Client;
import org.atlasapi.s3.S3Client;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.RepetitionRules.Every;
import com.metabroadcast.common.scheduling.RepetitionRules.Weekly;
import com.metabroadcast.common.security.UsernameAndPassword;
import com.metabroadcast.common.time.DayOfWeek;

@Configuration
public class PaModule {
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    private final static Every REPEATED = RepetitionRules.every(Duration.standardHours(5));
    private final static Weekly WEEKLY = RepetitionRules.weekly(DayOfWeek.FRIDAY, new LocalTime(22, 0, 0));
    
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
        scheduler.schedule(paCompleteUpdater(), WEEKLY);
        scheduler.schedule(paFileUpdater(), REPEATED);
        scheduler.schedule(paRecentUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("PA update scheduled task installed").withSource(PaCompleteUpdater.class));
    }
    
    @Bean PaFtpFileUpdater ftpFileUpdater() {
        return new PaFtpFileUpdater(ftpHost, new UsernameAndPassword(ftpUsername, ftpPassword), ftpPath, paProgrammeDataStore(), log);
    }
    
    @Bean PaProgrammeDataStore paProgrammeDataStore() {
        S3Client s3client = new DefaultS3Client(s3access, s3secret, "pa-data");
        return new DefaultPaProgrammeDataStore(localFilesPath, s3client);
    }
    
    @Bean PaProgrammeProcessor paProgrammeProcessor() {
        return new PaProgrammeProcessor(contentWriter, contentResolver, log);
    }
    
    @Bean PaCompleteUpdater paCompleteUpdater() {
        return new PaCompleteUpdater(paProgrammeProcessor(), paProgrammeDataStore(), log);
    }
    
    @Bean PaRecentUpdater paRecentUpdater() {
        return new PaRecentUpdater(paProgrammeProcessor(), paProgrammeDataStore(), log);
    }
    
    @Bean PaFileUpdater paFileUpdater() {
        return new PaFileUpdater(ftpFileUpdater(), log);
    }
    
    public @Bean PaSingleDateUpdatingController paUpdateController() {
        return new PaSingleDateUpdatingController(paProgrammeProcessor(), log, paProgrammeDataStore());
    }
}
