package org.atlasapi.remotesite.lovefilm;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.jets3t.service.security.AWSCredentials;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class LoveFilmModule {
    
    private final static RepetitionRule DAILY = RepetitionRules.daily(new LocalTime(23, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed LoveFilm updater"));
        scheduler.schedule(loveFilmCsvUpdater().withName("LoveFilm CSV Updater"), DAILY);
    }
    
    private LoveFilmCsvUpdateTask loveFilmCsvUpdater() {
        String s3access = Configurer.get("lovefilm.s3.access").get();
        String s3secret = Configurer.get("lovefilm.s3.secret").get();
        String s3bucket = Configurer.get("lovefilm.s3.bucket").get();
        String s3folder = Configurer.get("lovefilm.s3.folder").get();
        AWSCredentials credentials = new AWSCredentials(s3access, s3secret);
        RestS3ServiceSupplier serviceSupplier = new RestS3ServiceSupplier(credentials);
        LoveFilmDataSupplier dataSupplier = new S3LoveFilmDataSupplier(serviceSupplier, s3bucket, s3folder);
        LoveFilmDataRowHandler dataHandler = new DefaultLoveFilmDataRowHandler(contentResolver, contentWriter);
        return new LoveFilmCsvUpdateTask(dataSupplier, dataHandler);
    }
}
