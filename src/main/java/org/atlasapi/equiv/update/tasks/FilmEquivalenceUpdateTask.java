package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.media.entity.Publisher.RADIO_TIMES;
import static org.atlasapi.persistence.content.ContentCategory.TOP_LEVEL_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;


public class FilmEquivalenceUpdateTask extends AbstractContentEquivalenceUpdateTask<Film> {

    private static final String schedulingKey = "film-equivalence";
    private final ContentLister contentLister;
    private final ContentEquivalenceUpdater<Film> updater;
    
    public FilmEquivalenceUpdateTask(ContentLister contentLister, ContentEquivalenceUpdater<Film> updater, AdapterLog log, ScheduleTaskProgressStore progressStore) {
        super(log, progressStore);
        this.contentLister = contentLister;
        this.updater = updater;
    }

    @Override
    protected Iterator<Film> getContentIterator(ContentListingProgress progress) {
        Iterator<Content> lister = contentLister.listContent(defaultCriteria().forContent(TOP_LEVEL_ITEM).forPublisher(RADIO_TIMES).startingAt(progress).build());
        return Iterators.filter(lister, Film.class);
    }

    @Override
    protected String schedulingKey() {
        return schedulingKey;
    }

    @Override
    protected void handle(Film content) {
        updater.updateEquivalences(content, Optional.<List<Film>>absent());
    }

}
