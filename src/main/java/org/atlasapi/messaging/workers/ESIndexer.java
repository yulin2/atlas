package org.atlasapi.messaging.workers;

import java.util.Arrays;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentIndexer;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESIndexer extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //
    private final ContentResolver contentResolver;
    private final ContentIndexer contentIndexer;

    public ESIndexer(ContentResolver contentResolver, ContentIndexer contentIndexer) {
        this.contentResolver = contentResolver;
        this.contentIndexer = contentIndexer;
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        // TODO_SB : ResolvedContent should be eliminated or made way simpler:
        ResolvedContent results = contentResolver.findByCanonicalUris(Arrays.asList(message.getEntityId()));
        //
        if (results.getAllResolvedResults().size() > 1) {
            throw new IllegalStateException("More than one content found for id: " + message.getEntityId());
        } else if (results.getAllResolvedResults().size() == 1) {
            Identified source = results.getFirstValue().requireValue();
            if (source instanceof Item) {
                contentIndexer.index((Item) source);
            } else if (source instanceof Container) {
                contentIndexer.index((Container) source);
            } else {
                log.warn("Unexpected type {} found for id {} on message of type {} and id {}.",
                        new Object[]{source.getClass().getName(), message.getEntityId(), message.getClass().getName(), message.getMessageId()});
            }
        } else {
            log.warn("No content found for id {} on message of type {} and id {}.",
                    new Object[]{message.getEntityId(), message.getClass().getName(), message.getMessageId()});
        }
    }
}