package org.atlasapi.remotesite.wikipedia;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.atlasapi.media.entity.Film;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.wikipedia.film.FilmArticleTitleSource;
import org.atlasapi.remotesite.wikipedia.film.FilmExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * A task which iterates over all films from a {@link FilmArticleTitleSource}, adding each of them in turn.
 */
public class FilmsUpdater extends ScheduledTask {
    private static Logger log = LoggerFactory.getLogger(FilmsUpdater.class);
    private FilmArticleTitleSource titleSource;
    private ArticleFetcher fetcher;
    private FilmExtractor extractor;
    
    private ContentWriter writer;
    private UpdateProgress progress;
    private Optional<Integer> totalTitles = Optional.absent();
    
    public FilmsUpdater(FilmArticleTitleSource titleSource, ArticleFetcher fetcher, FilmExtractor extractor, ContentWriter writer) {
        this.titleSource = checkNotNull(titleSource);
        this.fetcher = checkNotNull(fetcher);
        this.extractor = checkNotNull(extractor);
        this.writer = checkNotNull(writer);
    }

    @Override
    protected void runTask() {
        reportStatus("Starting...");
        progress = UpdateProgress.START;
        Iterable<String> titles = titleSource.getAllFilmArticleTitles();
        totalTitles = (Optional<Integer>) ((titles instanceof Collection) ? Optional.of(((Collection) titles).size()) : Optional.absent());
        
        for (String title : titles) {
            try {
                Article article = fetcher.fetchArticle(title);

                log.info("Processing film article \"" + title + "\"");
                Film flim = extractor.extract(article);
                writer.createOrUpdate(flim);
                reduceProgress(UpdateProgress.SUCCESS);
            } catch (Exception e) {
                log.warn("Failed to correctly extract the film \"" + title + "\" from Wikipedia", e);
                reduceProgress(UpdateProgress.FAILURE);
            }
        }
        reportStatus(String.format("Processed: %d films (%d failed)", progress.getTotalProgress(), progress.getFailures()));
    }
    
    private void reduceProgress(UpdateProgress occurrence) {
        synchronized (this) {
            progress = progress.reduce(occurrence);
        }
        if (totalTitles.isPresent()) {
            reportStatus(String.format("Processing: %d/%d films so far (%d failed)", progress.getTotalProgress(), totalTitles.get(), progress.getFailures()));
        } else {
            reportStatus(String.format("Processing: %d films so far (%d failed)", progress.getTotalProgress(), progress.getFailures()));
        }
    }
}
