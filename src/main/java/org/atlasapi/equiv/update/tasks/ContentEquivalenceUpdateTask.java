
package org.atlasapi.equiv.update.tasks;


import static org.atlasapi.media.content.util.ContentCategory.CONTAINER;
import static org.atlasapi.media.content.util.ContentCategory.TOP_LEVEL_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ContentEquivalenceUpdateTask extends AbstractContentEquivalenceUpdateTask<Content> {

    private final ContentLister contentStore;
    
    private String schedulingKey = "equivalence";
    private List<Publisher> publishers;

    private final ContentEquivalenceUpdater<Content> rootUpdater;
    
    public ContentEquivalenceUpdateTask(ContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, ScheduleTaskProgressStore progressStore) {
        super(log, progressStore);
        this.contentStore = contentStore;
        this.rootUpdater = rootUpdater;
    }
    
    @Override
    protected Iterator<Content> getContentIterator(ContentListingProgress progress) {
        return contentStore.listContent(defaultCriteria().forPublishers(publishers).forContent(CONTAINER, TOP_LEVEL_ITEM).startingAt(progress).build());
    }

    public ContentEquivalenceUpdateTask forPublishers(Publisher... publishers) {
        this.publishers = ImmutableList.copyOf(publishers);
        this.schedulingKey = Joiner.on("-").join(Iterables.transform(this.publishers, Publisher.TO_KEY))+"-equivalence";
        return this;
    }

    @Override
    protected String schedulingKey() {
        return schedulingKey;
    }

    @Override
    protected void handle(Content content) {
        rootUpdater.updateEquivalences(content, Optional.<List<Content>>absent());
    }

}
