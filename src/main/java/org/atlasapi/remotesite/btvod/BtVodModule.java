package org.atlasapi.remotesite.btvod;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

public class BtVodModule {
    private final static Daily DAILY = RepetitionRules.daily(new LocalTime(0, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter writer;
    private @Autowired ContentResolver resolver;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(bTVODUpdater().withName("BT VOD Updater"), DAILY);
    }
    
    private BtVodUpdater bTVODUpdater() {
        String url = Configurer.get("bt.url").get();
        String username = Configurer.get("bt.username").get();
        String password = Configurer.get("bt.password").get();
        int timeout = Configurer.get("bt.url").toInt();
        BtVodContentCreator<Film> filmCreator = new BtVodFilmCreator();
        BtVodContentCreator<Episode> episodeCreator = new BtVodEpisodeCreator();
        BtVodContentCreator<Series> seriesCreator = new BtVodSeriesCreator();
        
        BtVodItemDataExtractor itemDataExtractor = new BtVodItemDataExtractor();
        BtVodContentExtractor extractor = new BtVodContentExtractor(filmCreator, episodeCreator, seriesCreator, itemDataExtractor);
        BtVodXmlElementHandler elementHandler = new DefaultBtVodXmlElementHandler(extractor, resolver, writer);
        
        BtVodContentFetcher fetcher = new BtVodContentFetcher(url, username, password, timeout);
        return new BtVodUpdater(elementHandler, fetcher);
    }
}
