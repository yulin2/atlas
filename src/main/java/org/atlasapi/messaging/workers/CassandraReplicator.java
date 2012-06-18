package org.atlasapi.messaging.workers;

import java.util.Arrays;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CassandraReplicator extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //
    private final ContentResolver mongoContentResolver;
    private final ContentWriter cassandraContentWriter;

    public CassandraReplicator(ContentResolver mongoContentResolver, ContentWriter cassandraContentWriter) {
        this.mongoContentResolver = mongoContentResolver;
        this.cassandraContentWriter = cassandraContentWriter;
    }

    @Override
    public void process(EntityUpdatedEvent event) {
        ResolvedContent results = mongoContentResolver.findByCanonicalUris(Arrays.asList(event.getEntityId()));
        if (results.getAllResolvedResults().size() > 1) {
            throw new IllegalStateException("More than one content found for id: " + event.getEntityId());
        } else if (results.getAllResolvedResults().size() == 1) {
            Identified source = results.getFirstValue().requireValue();
            if (source instanceof Container) {
                cassandraContentWriter.createOrUpdate((Container) source);
            } else if (source instanceof Item) {
                cassandraContentWriter.createOrUpdate((Item) source);
            } else {
                log.warn("Unexpected type {} found for id {} on event of type {} and id {}.",
                        new Object[]{source.getClass().getName(), event.getEntityId(), event.getClass().getName(), event.getChangeId()});
            }
        } else {
            log.warn("No content found for id {} on event of type {} and id {}.",
                    new Object[]{event.getEntityId(), event.getClass().getName(), event.getChangeId()});
        }
    }
}
