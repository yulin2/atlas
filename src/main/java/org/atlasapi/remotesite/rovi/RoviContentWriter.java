package org.atlasapi.remotesite.rovi;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;


/**
 * Is the responsible for writing a content to Atlas. It internally delegates the writing to a {@link ContentWriter}
 *
 */
public class RoviContentWriter {
    
    private final ContentWriter contentWriter;
    
    public RoviContentWriter(ContentWriter contentWriter) {
        this.contentWriter = contentWriter;
    }
    
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
        }
    }

}
