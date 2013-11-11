package org.atlasapi.remotesite.wikipedia;

import com.metabroadcast.common.scheduling.SimpleScheduler;
import javax.annotation.PostConstruct;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public class WikipediaModule {
    private static final Logger log = LoggerFactory.getLogger(WikipediaModule.class);
    static final Publisher SOURCE = Publisher.WIKIPEDIA;

    private @Autowired SimpleScheduler scheduler;
    private @Value("${updaters.wikipedia.enabled}") Boolean tasksEnabled;

	private @Autowired @Qualifier("contentResolver") ContentResolver contentResolver;
	private @Autowired @Qualifier("contentWriter") ContentWriter contentWriter;
    
    private final EnglishWikipediaClient ewc = new EnglishWikipediaClient();
    protected final ArticleFetcher fetcher = ewc;
    protected final FilmExtractor filmExtractor = new FilmExtractor();
    protected final FilmArticleTitleSource titleSource = ewc;
    
    @PostConstruct
    public void setUp() {
        if(tasksEnabled) {
//            scheduler.schedule(null, null); // TODO schedule some things
            log.info("Wikipedia update scheduled tasks installed");
        }
    }
    
    @Bean
    public WikipediaUpdatesController updatesController() {
        return new WikipediaUpdatesController(this);
    }
    
    public FilmsUpdater allFilmsUpdater() {
        return new FilmsUpdater(titleSource, fetcher, filmExtractor, contentResolver, contentWriter);
    }
}
