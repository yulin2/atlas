package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Content;

public interface NetflixContentExtractor<T extends Content> {
    
    Set<T> extract(Element source, int id);
}
