package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.persistence.content.ContentCategory.TOP_LEVEL_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;
<<<<<<< HEAD
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;
=======
>>>>>>> new content lister

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

    private static final String schedulingKey = "film-equivalence";
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
        try {
            ContentListingProgress currentProgress = progressStore.progressForTask(schedulingKey);
            log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Start equivalence task from %s", startProgress(currentProgress)));
            
            ContentListingCriteria criteria = defaultCriteria().forContent(TOP_LEVEL_ITEM).forPublisher(Publisher.PA).startingAt(currentProgress).build();
            Iterator<Film> films = Iterators.filter(contentLister.listContent(criteria), Film.class);
            
            int processed = 0;
            int failures = 0;
            boolean shouldContinue = shouldContinue();
            Film film = null;
            
            while (shouldContinue && films.hasNext()) {
                try {
                    film = films.next();
                    reportStatus(String.format("Processed %d. %d failures. Processing %s", processed, failures, film.getCanonicalUri()));
                    
                    rootUpdater.updateEquivalences(film);
                    
                    if (++processed % 100 == 0) {
                        progressStore.storeProgress(schedulingKey, progressFrom(film));
                    }
                    shouldContinue = shouldContinue();
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+film.getCanonicalUri()));
                    failures++;
                }
            }
            
            if(shouldContinue) {
                progressStore.storeProgress(schedulingKey, ContentListingProgress.START);
                reportStatus(String.format("Finshed. Processed %d. %d failures.", processed, failures));
                log.record(infoEntry().withSource(getClass()).withDescription("Finished: %s", schedulingKey));
            } else {
                if(film != null) {
                    progressStore.storeProgress(schedulingKey, progressFrom(film));
                    log.record(infoEntry().withSource(getClass()).withDescription("Stopped: %s at %s", schedulingKey, film.getCanonicalUri()));
                    reportStatus(String.format("Stopped. Processed %d. %d failures.", processed, failures));
                }
            }
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception running task " + schedulingKey));
        }
    }

    private String startProgress(ContentListingProgress progress) {
        if (progress == null || ContentListingProgress.START.equals(progress)) {
            return "start";
        }
        return String.format("%s %s %s", progress.getCategory(), progress.getPublisher(), progress.getUri());
    }

}
