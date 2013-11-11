package org.atlasapi.remotesite.wikipedia;

import com.metabroadcast.common.scheduling.ScheduledTask;
import org.atlasapi.media.entity.Film;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task which iterates over all films from a {@link FilmArticleTitleSource}, adding each of them in turn.
 */
public class FilmsUpdater extends ScheduledTask {
    private static Logger log = LoggerFactory.getLogger(FilmsUpdater.class);
    private FilmArticleTitleSource titleSource;
    private ArticleFetcher fetcher;
    private FilmExtractor extractor;
    
    private ContentResolver resolver;
    private ContentWriter writer;
    
    public FilmsUpdater(FilmArticleTitleSource titleSource, ArticleFetcher fetcher, FilmExtractor extractor, ContentResolver resolver, ContentWriter writer) {
        this.titleSource = titleSource;
        this.fetcher = fetcher;
        this.extractor = extractor;
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    protected void runTask() {
        for (String title : titleSource.getAllFilmArticleTitles()) {
            try {
                log.info("Processing film article \"" + title + "\"");
                Film flim = extractor.extract(fetcher.fetchArticle(title));
                writer.createOrUpdate(flim);
            } catch (Exception e) {
                log.warn("Failed to correctly extract the film \"" + title + "\" from Wikipedia", e);
            }
        }
    }
}
