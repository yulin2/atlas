package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;


public class SimilarContentUpdater extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(SimilarContentUpdater.class);
    
    private final ContentLister contentLister;
    private final Publisher publisher;
    private final SimilarContentProvider similarContentProvider;

    public SimilarContentUpdater(ContentLister contentLister, Publisher publisher, 
            SimilarContentProvider similarContentProvider) {
        
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.similarContentProvider = checkNotNull(similarContentProvider);
    }
    
    @Override
    public void runTask() {
        ContentListingCriteria criteria = new ContentListingCriteria.Builder()
                                                    .forPublisher(publisher)
                                                    .forContent(ContentCategory.TOP_LEVEL_CONTENT)
                                                    .build();
        Iterator<Content> content = contentLister.listContent(criteria);
        UpdateProgress progress = UpdateProgress.START;
        while (content.hasNext()) {
            Content c = content.next();
            try {
                List<Described> similar = similarContentProvider.similarTo(c);
                log.trace("Similar to [{} : {}] are the following:", c.getCanonicalUri(), c.getTitle());
                for (Described d : similar) {
                    log.trace("{} : {}", d.getCanonicalUri(), d.getTitle());
                }
                progress = progress.reduce(UpdateProgress.SUCCESS);
            } catch (Exception e) {
                log.error(String.format("Content %s failed", c.getCanonicalUri()), e);
                progress = progress.reduce(UpdateProgress.FAILURE);
            }
            reportProgress(progress);
        }
    }
    
    private void reportProgress(UpdateProgress progress) {
        reportStatus(String.format("%d proceseed : %d successes, %d failures", 
                                        progress.getTotalProgress(),
                                        progress.getProcessed(),
                                        progress.getFailures()
                                  )
        );
    }
}
