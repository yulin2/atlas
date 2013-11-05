package org.atlasapi.remotesite.channel4.pmlsd;

import org.atlasapi.media.entity.Item;
import org.jdom.Element;

import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

public abstract class C4MediaItemExtractor<I extends Item> extends BaseC4ItemExtractor<I> {

    public C4MediaItemExtractor(Clock clock) {
        super(clock);
    }
    
    @Override
    protected Element getMedia(Entry source) {
        return C4AtomApi.mediaGroup(source);
    }

}