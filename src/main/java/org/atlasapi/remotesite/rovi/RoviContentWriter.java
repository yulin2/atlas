package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;


/**
 * This is responsible for writing a content to Atlas. It internally delegates the writing to a {@link ContentWriter}
 *
 */
public class RoviContentWriter {
    
    private final ContentWriter contentWriter;
    
    public RoviContentWriter(ContentWriter contentWriter) {
        this.contentWriter = checkNotNull(contentWriter);
    }
    
    public void writeContent(Content content) {
        if (content instanceof Container) {
            contentWriter.createOrUpdate((Container) content);
        } else if (content instanceof Item) {
            contentWriter.createOrUpdate((Item) content);
        } else {
            throw new RuntimeException("Unexpected instance type: " + content.getClass().getName());
        }
    }

}
