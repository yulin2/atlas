package org.atlasapi.remotesite.pa.film;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.lookup.LookupWriter;
import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

public class PaFilmModule {

    private final static Daily DAILY = RepetitionRules.daily(new LocalTime(5, 30, 0));

    @Value("${pa.film.feedUrl}")
    private String feedUrl;

    @Autowired private SimpleScheduler scheduler;
    @Autowired private AdapterLog log;
    @Autowired private ContentWriters contentWriter;
    @Autowired private ContentResolver contentResolver;
    @Autowired private ItemsPeopleWriter peopleWriter;
    @Autowired private LookupWriter lookupWriter;
    @Autowired private SearchResolver searchResolver;

    @PostConstruct
    public void startUp() {
        scheduler.schedule(paFilmFeedDeltaUpdater().withName("PA Film Feed Updater"), DAILY);
        scheduler.schedule(paFilmFeedCompleteUpdater().withName("PA Film Feed Complete Updater"), RepetitionRules.NEVER);
    }

    @Bean
    public PaFilmFeedUpdater paFilmFeedDeltaUpdater() {
        return new PaFilmFeedUpdater(feedUrl, log, contentResolver, contentWriter, paFilmProcessor());
    }
    
    @Bean
    public PaFilmFeedUpdater paFilmFeedCompleteUpdater() {
        return PaFilmFeedUpdater.completeUpdater(feedUrl, log, contentResolver, contentWriter, paFilmProcessor());
    }
    
    @Bean
    public FilmEquivUpdater filmEquivUpdater() {
        return new FilmEquivUpdater(searchResolver, lookupWriter);
    }
    
    @Bean
    public PaFilmProcessor paFilmProcessor() {
        return new PaFilmProcessor(contentResolver, contentWriter, peopleWriter,filmEquivUpdater(), log);
    }
}
