package org.atlasapi.remotesite.wikipedia;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;
import net.sourceforge.jwbf.core.contentRep.Article;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
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
                Article article = fetcher.fetchArticle(title);
                Maybe<Identified> storedVersion = resolver.findByCanonicalUris(ImmutableList.of(extractor.urlFor(article))).getFirstValue();
                if(storedVersion.hasValue()
                  && storedVersion.valueOrNull().getLastUpdated() != null
                  && !storedVersion.valueOrNull().getLastUpdated().isBefore(extractor.lastModifiedTimeFor(article))) {
                    log.info("Skipping up-to-date film article \"" + title + "\"");
                    continue;
                }
                log.info("Processing film article \"" + title + "\"");
                Film flim = extractor.extract(article);
                writer.createOrUpdate(flim);
            } catch (Exception e) {
                log.warn("Failed to correctly extract the film \"" + title + "\" from Wikipedia", e);
            }
        }
    }
}
