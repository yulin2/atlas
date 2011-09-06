package org.atlasapi.equiv.update.tasks;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

public class PublisherListingProgress extends ContentListingProgress {

    private final Publisher publisher;
    
    public PublisherListingProgress(ContentListingProgress progress, Publisher publisher) {
        this(progress.getUri(), progress.getTable(), publisher);
        this.withCount(progress.count());
        this.withTotal(progress.total());
    }

    public PublisherListingProgress(String uri, ContentTable table, Publisher publisher) {
        super(uri, table);
        this.publisher = publisher;
    }

    public Publisher getPublisher() {
        return publisher;
    }

}
