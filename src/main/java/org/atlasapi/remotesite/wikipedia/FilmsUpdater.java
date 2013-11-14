package org.atlasapi.remotesite.wikipedia;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
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
        this.titleSource = checkNotNull(titleSource);
        this.fetcher = checkNotNull(fetcher);
        this.extractor = checkNotNull(extractor);
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
    }

    @Override
    protected void runTask() {
        Iterable<String> titles = titleSource.getAllFilmArticleTitles();
        for (String title : titles) {
            try {
                Article article = fetcher.fetchArticle(title);

                log.info("Processing film article \"" + title + "\"");
                Film flim = extractor.extract(article);
                writer.createOrUpdate(flim);
            } catch (Exception e) {
                log.warn("Failed to correctly extract the film \"" + title + "\" from Wikipedia", e);
            }
        }
    }
}
