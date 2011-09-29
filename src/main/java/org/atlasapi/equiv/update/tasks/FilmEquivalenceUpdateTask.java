package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.persistence.content.ContentCategory.TOP_LEVEL_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;

import java.util.Iterator;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.collect.Iterators;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class FilmEquivalenceUpdateTask extends ScheduledTask {

    private static final String schedulingKey = "filmEquivalence";
    private final ContentLister contentLister;
    private final ContentEquivalenceUpdater<Film> rootUpdater;
    private final AdapterLog log;
    private final ScheduleTaskProgressStore progressStore;
    
    public FilmEquivalenceUpdateTask(ContentLister contentLister, ContentEquivalenceUpdater<Film> updater, AdapterLog log, ScheduleTaskProgressStore progressStore) {
        this.contentLister = contentLister;
        this.rootUpdater = updater;
        this.log = log;
        this.progressStore = progressStore;
    }
    
    @Override
    protected void runTask() {
        ContentListingProgress currentProgress = progressStore.progressForTask(schedulingKey);
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Start equivalence task from %s", startProgress(currentProgress)));
        
        ContentListingCriteria criteria = defaultCriteria().forContent(TOP_LEVEL_ITEM).forPublisher(Publisher.PA).startingAt(currentProgress).build();
        Iterator<Film> films = Iterators.filter(contentLister.listContent(criteria), Film.class);
        
        int processed = 0;
        boolean shouldContinue = shouldContinue();
        Film film = null;
        
        while (shouldContinue && films.hasNext()) {
            film = films.next();
            reportStatus(String.format("Processed %d. Processing %s", processed, film.getCanonicalUri()));
            try {
                rootUpdater.updateEquivalences(film);
            } catch (Exception e) {
                log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+film.getCanonicalUri()));
            }
            if(++processed % 100 == 0) {
                progressStore.storeProgress(schedulingKey, progressFrom(film));
            }
            shouldContinue = shouldContinue();
        }
        
        if(shouldContinue) {
            progressStore.storeProgress(schedulingKey, ContentListingProgress.START);
        } else {
            if(film != null) {
                progressStore.storeProgress(schedulingKey, progressFrom(film));
            }
        }

        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finished film equivalence task")));
    }

    private String startProgress(ContentListingProgress progress) {
        return progress == null ? "start" : String.format("%s %s %s", progress.getCategory(), progress.getPublisher(), progress.getUri());
    }

}
