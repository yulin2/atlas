package org.atlasapi.remotesite.rovi;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;


/**
 * This is responsible for writing a content to Atlas. It internally delegates the writing to a {@link ContentWriter}
 *
 */
public class RoviContentWriter {
    
    private final ContentWriter contentWriter;
    
    public RoviContentWriter(ContentWriter contentWriter) {
        this.contentWriter = contentWriter;
    }
    
    // TODO: merge content if instance of item, in order to keep broadcasts
    public void writeContent(Content content) {
        if (content instanceof Brand) {
            contentWriter.createOrUpdate((Brand) content);
        } else if (content instanceof Series) {
            contentWriter.createOrUpdate((Series) content);
        } else if (content instanceof Episode) {
            contentWriter.createOrUpdate((Episode) content);
        } else if (content instanceof Film) {
            contentWriter.createOrUpdate((Film) content);
        } else if (content instanceof Item) {
            contentWriter.createOrUpdate((Item) content);
        } else {
            throw new RuntimeException("Unexpected instance type: " + content.getClass().getName());
        }
    }

}
