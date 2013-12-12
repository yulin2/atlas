package org.atlasapi.remotesite.channel4.pmlsd;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;


public interface C4UriExtractor<B, S, E> {
    
    public Optional<String> uriForBrand(Publisher publisher, B remote);
    public Optional<String> uriForSeries(Publisher publisher, S remote);
    public Optional<String> uriForItem(Publisher publisher, E remote);
    public Optional<String> uriForClip(Publisher publisher, E remote);

}
