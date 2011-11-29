package org.atlasapi.remotesite.itunes.epf;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Location;
import org.atlasapi.remotesite.itunes.epf.model.EpfVideo;

public class ItunesEpfVideoSource {

    private final EpfVideo video;
    private final Iterable<Location> locations;

    public ItunesEpfVideoSource(EpfVideo video, Iterable<Location> locations) {
        this.video = checkNotNull(video);
        this.locations = locations;
    }

    public EpfVideo video() {
        return this.video;
    }

    public Iterable<Location> locations() {
        return locations;
    }

}
