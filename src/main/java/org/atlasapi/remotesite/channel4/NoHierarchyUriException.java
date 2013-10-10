package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.entity.Episode;

public class NoHierarchyUriException extends Exception {

    private Episode episode;

    public NoHierarchyUriException(Episode episode) {
        this.episode = episode;
    }

    @Override
    public String getMessage() {
        return episode.getCanonicalUri();
    }
    
    @Override
    public String toString() {
        return String.format("no hierarchy uri %s", getMessage());
    }
    
}