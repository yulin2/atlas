package org.atlasapi.remotesite.channel4.pmlsd;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;

public class SourceSpecificContentFactory {

    private final Publisher source;

    public SourceSpecificContentFactory(Publisher source) {
        this.source = checkNotNull(source);
    }
    
    public Brand createBrand() {
        return setSource(new Brand());
    }
    
    public Clip createClip() {
        return setSource(new Clip());
    }
    
    public Episode createEpisode() {
        return setSource(new Episode());
    }
    
    public Item createItem() {
        return setSource(new Item());
    }
    
    public Series createSeries() {
        return setSource(new Series());
    }

    private <C extends Content> C setSource(C c) {
        c.setPublisher(source);
        return c;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof SourceSpecificContentFactory) {
            SourceSpecificContentFactory other = (SourceSpecificContentFactory) that;
            return source.equals(other.source);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return source.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) content factory", source.title(), source.key());
    }

}
