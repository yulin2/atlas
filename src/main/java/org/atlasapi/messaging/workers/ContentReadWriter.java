package org.atlasapi.messaging.workers;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ContentReadWriter extends AbstractWorker {

    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public ContentReadWriter(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(message.getEntityId()));
        for (Content content : Iterables.filter(resolved.getAllResolvedResults(), Content.class)) {
            content.setReadHash(null);//force write
            if (content instanceof Container) {
                contentWriter.createOrUpdate((Container)content);
            } else if (content instanceof Item) {
                contentWriter.createOrUpdate((Item)content);
            }
        }
    }
    
}
