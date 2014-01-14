package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;


public class SimilarContentUpdater extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(SimilarContentUpdater.class);
    
    private final ContentLister contentLister;
    private final Publisher publisher;
    private final SimilarContentProvider similarContentProvider;
    private final SimilarContentWriter similarContentWriter;
    

    public SimilarContentUpdater(ContentLister contentLister, Publisher publisher, 
            SimilarContentProvider similarContentProvider, SimilarContentWriter similarContentWriter) {
        
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.similarContentProvider = checkNotNull(similarContentProvider);
        this.similarContentWriter = checkNotNull(similarContentWriter);
    }
    
    @Override
    public void runTask() {
        ContentListingCriteria criteria = new ContentListingCriteria.Builder()
                                                    .forPublisher(publisher)
                                                    .forContent(ContentCategory.TOP_LEVEL_CONTENT)
                                                    .build();
        similarContentProvider.initialise();
        Iterator<Content> content = contentLister.listContent(criteria);
        UpdateProgress progress = UpdateProgress.START;
        while (content.hasNext()) {
            Content c = content.next();
            try {
                List<ChildRef> similar = similarContentProvider.similarTo(c);
                log.trace("Similar to [{} : {}] are the following:", c.getCanonicalUri(), c.getTitle());
                similarContentWriter.write(c, similar);
                for (ChildRef d : similar) {
                    log.trace("{}", d);
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
