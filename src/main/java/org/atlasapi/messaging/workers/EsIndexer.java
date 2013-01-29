package org.atlasapi.messaging.workers;

import java.util.Arrays;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.ContentIndexer;
import javax.jms.ConnectionFactory;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.util.Resolved;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.IndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class EsIndexer extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //
    private final ContentStore contentStore;
    private final ContentIndexer contentIndexer;

    public EsIndexer(ContentStore contentResolver, ContentIndexer contentIndexer) {
        this.contentStore = contentResolver;
        this.contentIndexer = contentIndexer;
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        // TODO_SB : ResolvedContent should be eliminated or made way simpler:
        Resolved<Content> results = contentStore.resolveIds(ImmutableList.of(Id.valueOf(message.getEntityId())));
        //
        Optional<Content> content = results.getResources().first();
        if (content.isPresent()) {
            Content source = content.get();
            log.info("Indexing {}", source);
            if (source instanceof Item) {
                try {
                    contentIndexer.index((Item) source);
                } catch (IndexException ie) {
                    log.error("Error indexing " + source, ie);
                }
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